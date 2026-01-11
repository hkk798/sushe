package org.example.service;

import org.example.entity.Student;
import org.example.entity.AllocationRecord;
import org.example.entity.User;
import org.example.mapper.StudentMapper;
import org.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取学生主页数据
     */
    public Map<String, Object> getStudentDashboardData(Integer studentId) {
        Map<String, Object> dashboardData = new HashMap<>();

        try {
            // 1. 获取学生基本信息
            Student student = studentMapper.findStudentWithUserInfo(studentId);
            if (student == null) {
                throw new RuntimeException("学生信息不存在");
            }

            dashboardData.put("student", student);

            // 2. 获取宿舍分配信息
            AllocationRecord allocation = studentMapper.findCurrentDormAllocation(studentId);
            if (allocation != null && allocation.getRoom() != null) {
                Map<String, Object> dormInfo = new HashMap<>();

                // === 放入完整的 Room 对象 (修复 my_dorm.html 报错的关键) ===
                dormInfo.put("room", allocation.getRoom());

                // === 放入扁平化字段 (兼容 student_index.html) ===
                dormInfo.put("roomNumber", allocation.getRoom().getRoomNo());
                dormInfo.put("floor", allocation.getRoom().getFloor());
                dormInfo.put("roomType", allocation.getRoom().getGenderType());

                // === 放入床位号 (兼容两个页面的不同写法) ===
                dormInfo.put("bedNumber", allocation.getBedNo()); // index页面用
                dormInfo.put("bedNo", allocation.getBedNo());     // my_dorm页面用

                // === 放入费用信息 ===
                BigDecimal yearlyFee = allocation.getRoom().getYearlyFee();
                dormInfo.put("yearlyFee", yearlyFee); // index页面用

                if (yearlyFee != null) {
                    BigDecimal monthlyFee = yearlyFee.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
                    dormInfo.put("monthlyFee", monthlyFee);
                } else {
                    dormInfo.put("monthlyFee", BigDecimal.ZERO);
                }

                // === 放入楼栋名称 ===
                // AllocationRecord 实体中扩展了 buildingName 字段，MyBatis查询时已映射
                String buildingName = allocation.getBuildingName();
                dormInfo.put("buildingName", buildingName != null ? buildingName : "未知楼栋");

                dashboardData.put("dormInfo", dormInfo);

                // 3. 获取舍友信息
                List<Map<String, Object>> roommates = studentMapper.findRoommates(
                        allocation.getRoomId(), studentId);
                dashboardData.put("roommates", roommates);
            } else {
                dashboardData.put("dormInfo", null);
                dashboardData.put("roommates", null);
            }

            // 4. 获取待处理的报修单数量
            Integer processingRepairsCount = studentMapper.countProcessingRepairs(studentId);
            dashboardData.put("processingRepairsCount", processingRepairsCount != null ? processingRepairsCount : 0);

            // 5. 计算统计信息
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRoommates", dashboardData.get("roommates") != null ?
                    ((List<?>) dashboardData.get("roommates")).size() : 0);
            stats.put("hasDorm", dashboardData.get("dormInfo") != null);

            dashboardData.put("stats", stats);

            return dashboardData;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取学生主页数据失败: " + e.getMessage());
        }
    }

    // ... (下面的方法保持不变) ...
    public boolean validateStudentAccess(Integer userId, Integer studentId) {
        try {
            User user = userMapper.findById(userId);
            if (user == null || !"student".equals(user.getRole())) return false;
            Integer actualUserId = studentMapper.findUserIdByStudentId(studentId);
            return actualUserId != null && actualUserId.equals(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasDormAllocation(Integer studentId) {
        try {
            AllocationRecord allocation = studentMapper.findCurrentDormAllocation(studentId);
            return allocation != null && allocation.isActive();
        } catch (Exception e) {
            return false;
        }
    }

    public List<AllocationRecord> getDormHistory(Integer studentId) {
        try {
            return studentMapper.findDormHistory(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}