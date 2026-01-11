package org.example.service;

import org.example.entity.RepairOrder;
import org.example.entity.Room;
import org.example.mapper.RepairMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class RepairService {

    @Autowired
    private RepairMapper repairMapper;

    // 移除未使用的 DormService 注入
    // @Autowired
    // private DormService dormService;

    // 报修类别列表
    private static final List<String> REPAIR_CATEGORIES = Arrays.asList(
            "电路故障", "供水故障", "器材故障", "其他"
    );

    /**
     * 获取报修位置信息 (已修复：兼容新的 dormInfo 结构)
     */
    public String getRepairLocation(Map<String, Object> dormInfo) {
        if (dormInfo == null) {
            return "未知位置";
        }

        StringBuilder location = new StringBuilder();

        // 1. 获取楼栋信息
        Object buildingName = dormInfo.get("buildingName");
        if (buildingName != null) {
            location.append(buildingName);
        } else if (dormInfo.get("buildingNo") != null) {
            location.append(dormInfo.get("buildingNo")).append("楼");
        } else {
            location.append("未知楼栋");
        }

        location.append("-");

        // 2. 获取房间信息 (优先使用 roomNumber，兼容 roomNo，最后尝试从 Room 对象获取)
        Object roomNo = dormInfo.get("roomNumber"); // StudentService 中使用的是 roomNumber
        if (roomNo == null) {
            roomNo = dormInfo.get("roomNo");
        }

        if (roomNo != null) {
            location.append(roomNo).append("室");
        } else {
            // 尝试从 Room 实体对象获取 (如果 Map 中包含了 room 对象)
            Object roomObj = dormInfo.get("room");
            if (roomObj instanceof Room) {
                location.append(((Room) roomObj).getRoomNo()).append("室");
            } else {
                location.append("未知房间");
            }
        }

        return location.toString();
    }

    /**
     * 获取报修类别列表
     */
    public List<String> getRepairCategories() {
        return REPAIR_CATEGORIES;
    }

    /**
     * 验证报修输入
     */
    public String validateRepairInput(String category, String description, MultipartFile[] images) {
        // 验证类别
        if (!StringUtils.hasText(category) || !REPAIR_CATEGORIES.contains(category)) {
            return "请选择有效的报修类别";
        }

        // 验证描述（如果有）
        if (StringUtils.hasText(description) && description.length() > 500) {
            return "问题描述不能超过500个字符";
        }

        // 验证图片
        if (images != null) {
            int imageCount = 0;
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    imageCount++;

                    // 验证文件类型
                    String contentType = image.getContentType();
                    if (contentType == null ||
                            !(contentType.startsWith("image/jpeg") ||
                                    contentType.startsWith("image/png") ||
                                    contentType.startsWith("image/gif"))) {
                        return "只支持JPG、PNG、GIF格式的图片";
                    }

                    // 验证文件大小（最大5MB）
                    if (image.getSize() > 5 * 1024 * 1024) {
                        return "每张图片大小不能超过5MB";
                    }
                }
            }

            if (imageCount > 3) {
                return "最多只能上传3张图片";
            }
        }

        return null; // 验证通过
    }

    /**
     * 提交报修申请
     */
    public boolean submitRepair(Integer studentId, Integer roomId,
                                String category, String description,
                                MultipartFile[] images) {
        try {
            // 创建报修单对象
            RepairOrder repairOrder = new RepairOrder();
            repairOrder.setStudentId(studentId);
            repairOrder.setRoomId(roomId);
            repairOrder.setTitle("报修申请 - " + category);
            repairOrder.setCategory(category);

            // 设置描述（可以为空）
            repairOrder.setDescription(StringUtils.hasText(description) ? description : "");

            // 保存图片
            String imagePaths = null;
            if (images != null && images.length > 0) {
                List<String> paths = saveRepairImages(images);
                if (!paths.isEmpty()) {
                    imagePaths = String.join(",", paths);
                }
            }

            // 设置图片路径
            repairOrder.setImages(imagePaths);
            repairOrder.setStatus("pending");
            repairOrder.setSubmitTime(new java.util.Date());

            // 保存报修单到数据库
            int result = repairMapper.saveRepairOrder(repairOrder);
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 保存报修图片
     */
    private List<String> saveRepairImages(MultipartFile[] images) {
        List<String> paths = new ArrayList<>();

        try {
            // 创建保存目录
            String uploadDir = System.getProperty("user.dir") + "/uploads/repair";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            for (int i = 0; i < Math.min(images.length, 3); i++) {
                MultipartFile image = images[i];
                if (image != null && !image.isEmpty()) {
                    // 生成文件名
                    String originalFilename = image.getOriginalFilename();
                    String fileExtension = originalFilename != null && originalFilename.contains(".") ?
                            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
                    String filename = "repair_" + System.currentTimeMillis() + "_" + i + fileExtension;

                    // 保存文件
                    File file = new File(dir, filename);
                    image.transferTo(file);

                    // 记录路径 (注意：这里返回的是相对路径，前端需要配合 ResourceHandler 访问)
                    paths.add("repair/" + filename);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return paths;
    }

    /**
     * 获取学生的报修历史（支持按状态筛选）
     */
    public List<RepairOrder> getRepairHistoryByStudentId(Integer studentId, String status) {
        if (status == null || status.trim().isEmpty()) {
            status = "all";
        }
        return repairMapper.findRepairHistoryByStudentIdAndStatus(studentId, status);
    }

    /**
     * 获取各状态的报修单数量统计
     */
    public Map<String, Integer> getStatusCounts(Integer studentId) {
        Map<String, Object> counts = repairMapper.countRepairStatusByStudentId(studentId);

        Map<String, Integer> result = new HashMap<>();
        if (counts != null) {
            // 安全的类型转换
            result.put("all", counts.get("total_count") != null ? ((Number) counts.get("total_count")).intValue() : 0);
            result.put("pending", counts.get("pending_count") != null ? ((Number) counts.get("pending_count")).intValue() : 0);
            result.put("processing", counts.get("processing_count") != null ? ((Number) counts.get("processing_count")).intValue() : 0);
            result.put("completed", counts.get("completed_count") != null ? ((Number) counts.get("completed_count")).intValue() : 0);
        } else {
            result.put("all", 0);
            result.put("pending", 0);
            result.put("processing", 0);
            result.put("completed", 0);
        }

        return result;
    }

    /**
     * 获取报修单详情
     */
    public RepairOrder getRepairOrderById(Integer orderId) {
        return repairMapper.findRepairOrderById(orderId);
    }

    /**
     * 撤销报修单
     */
    public boolean cancelRepair(Integer orderId) {
        try {
            // 验证报修单是否存在且状态为待处理
            RepairOrder repairOrder = repairMapper.findRepairOrderSimpleById(orderId);
            if (repairOrder == null || !"pending".equals(repairOrder.getStatus())) {
                return false;
            }

            // 删除报修单
            int result = repairMapper.deleteRepairOrder(orderId);
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取状态显示文本
     */
    public String getStatusDisplayText(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "pending": return "待处理";
            case "processing": return "处理中";
            case "completed": return "已完成";
            default: return "未知";
        }
    }

    /**
     * 格式化时间显示
     */
    public String formatDateTime(Date date) {
        if (date == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public RepairOrder getRepairOrderDetail(Integer orderId) {
        try {
            // 获取基础信息
            RepairOrder repairOrder = repairMapper.findRepairOrderById(orderId);

            if (repairOrder != null) {
                // 可以在这里添加更多处理逻辑，比如：
                // 1. 获取宿舍信息
                // 2. 获取处理人信息
                // 3. 格式化时间
                // 4. 处理图片路径
            }

            return repairOrder;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取报修单图片路径列表
     */
    public List<String> getRepairOrderImages(Integer orderId) {
        try {
            RepairOrder repairOrder = repairMapper.findRepairOrderSimpleById(orderId);
            if (repairOrder == null || repairOrder.getImages() == null || repairOrder.getImages().isEmpty()) {
                return new ArrayList<>();
            }

            return Arrays.asList(repairOrder.getImages().split(","));

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public RepairOrder getLatestRepairByStudentId(Integer studentId) {
        try {
            // 先获取所有报修记录，然后取最新的一个
            List<RepairOrder> repairs = repairMapper.findRepairHistoryByStudentId(studentId);
            if (repairs != null && !repairs.isEmpty()) {
                return repairs.get(0); // 列表已按时间倒序排列，第一个就是最新的
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}