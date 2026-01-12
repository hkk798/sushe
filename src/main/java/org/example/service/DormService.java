package org.example.service;

import org.example.entity.*;
import org.example.mapper.DormMapper;
import org.example.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DormService {

    @Autowired
    private DormMapper dormMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 获取学生的宿舍信息
     */
    public Map<String, Object> getStudentDormInfo(Integer studentId) {
        try {
            AllocationRecord allocation = studentMapper.findCurrentDormAllocation(studentId);
            if (allocation == null) {
                return null;
            }

            Map<String, Object> dormInfo = new HashMap<>();

            // 基本分配信息
            dormInfo.put("allocation", allocation);
            dormInfo.put("roomId", allocation.getRoomId());
            dormInfo.put("bedNo", allocation.getBedNo());
            dormInfo.put("status", allocation.getStatus());
            dormInfo.put("feeStatus", allocation.getFeeStatus());
            dormInfo.put("allocateDate", allocation.getAllocateDate());
            dormInfo.put("checkInDate", allocation.getCheckInDate());

            // 房间信息
            if (allocation.getRoom() != null) {
                Room room = allocation.getRoom();
                dormInfo.put("room", room);
                dormInfo.put("roomNo", room.getRoomNo());
                dormInfo.put("floor", room.getFloor());
                dormInfo.put("capacity", room.getCapacity());
                dormInfo.put("currentCount", room.getCurrentCount());
                dormInfo.put("yearlyFee", room.getYearlyFee());
                dormInfo.put("genderType", room.getGenderType());
                dormInfo.put("roomStatus", room.getStatus());

                // 楼栋信息
                if (room.getBuildingId() != null) {
                    Building building = dormMapper.findBuildingById(room.getBuildingId());
                    dormInfo.put("building", building);
                    if (building != null) {
                        dormInfo.put("buildingName", building.getBuildingName());
                        dormInfo.put("buildingNo", building.getBuildingNo());
                    }
                }
            }

            return dormInfo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取舍友信息
     */
    public Map<String, Object> getRoommatesInfo(Integer roomId, Integer excludeStudentId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 使用StudentMapper的方法，它会自动排除当前用户
            List<Map<String, Object>> otherRoommates = studentMapper.findRoommates(roomId, excludeStudentId);

            result.put("roommatesList", otherRoommates != null ? otherRoommates : new ArrayList<>());
            result.put("totalCount", otherRoommates != null ? otherRoommates.size() : 0);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("roommatesList", new ArrayList<>());
            result.put("totalCount", 0);
        }

        return result;
    }

    /**
     * 获取宿舍统计信息
     */
    public Map<String, Object> getDormStats(Map<String, Object> dormInfo, Map<String, Object> roommatesInfo) {
        Map<String, Object> stats = new HashMap<>();

        if (dormInfo == null) {
            stats.put("hasDorm", false);
            stats.put("roomCapacity", 0);
            stats.put("totalRoommates", 0);
            stats.put("vacancyCount", 0);
            stats.put("isFull", true);
            return stats;
        }

        stats.put("hasDorm", true);

        // 房间容量
        Integer capacity = (Integer) dormInfo.get("capacity");
        stats.put("roomCapacity", capacity != null ? capacity : 0);

        // 当前人数
        Integer currentCount = (Integer) dormInfo.get("currentCount");
        stats.put("currentCount", currentCount != null ? currentCount : 0);

        // 舍友数量
        if (roommatesInfo != null) {
            List<?> roommatesList = (List<?>) roommatesInfo.get("roommatesList");
            int roommateCount = roommatesList != null ? roommatesList.size() : 0;
            stats.put("totalRoommates", roommateCount);

            // 空位计算（包括自己）
            int vacancy = (capacity != null ? capacity : 0) -
                    ((currentCount != null ? currentCount : 0) + 1); // +1 表示自己
            stats.put("vacancyCount", vacancy > 0 ? vacancy : 0);
            stats.put("isFull", vacancy <= 0);
        } else {
            stats.put("totalRoommates", 0);
            stats.put("vacancyCount", capacity != null ? capacity - 1 : 0); // -1 表示自己占了一个位置
            stats.put("isFull", false);
        }

        return stats;
    }

    /**
     * 检查宿舍状态警告
     */
    public String checkDormStatusWarnings(Map<String, Object> dormInfo) {
        if (dormInfo == null) {
            return null;
        }

        StringBuilder warning = new StringBuilder();

        // 检查缴费状态
        String feeStatus = (String) dormInfo.get("feeStatus");
        if ("overdue".equals(feeStatus)) {
            warning.append("住宿费已逾期，请尽快缴纳！");
        } else if ("pending".equals(feeStatus)) {
            warning.append("住宿费待缴纳，请在规定时间内完成缴费。");
        }

        // 检查分配状态
        String status = (String) dormInfo.get("status");
        if ("pending".equals(status)) {
            if (warning.length() > 0)
                warning.append("<br>");
            warning.append("宿舍分配待确认，请及时确认入住。");
        } else if ("completed".equals(status)) {
            if (warning.length() > 0)
                warning.append("<br>");
            warning.append("宿舍分配已完成，请联系管理员查看退宿详情。");
        }

        // 检查是否已入住
        Object checkInDate = dormInfo.get("checkInDate");
        if (checkInDate == null && "active".equals(status)) {
            if (warning.length() > 0)
                warning.append("<br>");
            warning.append("尚未办理入住手续，请尽快到宿舍楼办理入住。");
        }

        return warning.length() > 0 ? warning.toString() : null;
    }

    /**
     * 检查是否有宿舍分配
     */
    public boolean hasDormAllocation(Integer studentId) {
        try {
            Map<String, Object> dormInfo = getStudentDormInfo(studentId);
            return dormInfo != null && "active".equals(dormInfo.get("status"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取房间报修历史
     */
    public Map<String, Object> getRoomRepairHistory(Integer roomId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<RepairOrder> repairOrders = dormMapper.findRoomRepairHistory(roomId);

            // 按状态分类
            List<RepairOrder> pendingOrders = new ArrayList<>();
            List<RepairOrder> processingOrders = new ArrayList<>();
            List<RepairOrder> completedOrders = new ArrayList<>();

            for (RepairOrder order : repairOrders) {
                String status = order.getStatus();
                if (status == null)
                    continue;

                switch (status.toLowerCase()) {
                    case "pending":
                        pendingOrders.add(order);
                        break;
                    case "processing":
                        processingOrders.add(order);
                        break;
                    case "completed":
                        completedOrders.add(order);
                        break;
                }
            }

            result.put("allOrders", repairOrders);
            result.put("pendingOrders", pendingOrders);
            result.put("processingOrders", processingOrders);
            result.put("completedOrders", completedOrders);
            result.put("totalCount", repairOrders.size());
            result.put("pendingCount", pendingOrders.size());
            result.put("processingCount", processingOrders.size());
            result.put("completedCount", completedOrders.size());

        } catch (Exception e) {
            e.printStackTrace();
            result.put("allOrders", new ArrayList<>());
            result.put("totalCount", 0);
            result.put("pendingCount", 0);
            result.put("processingCount", 0);
            result.put("completedCount", 0);
        }

        return result;
    }

    /**
     * 获取楼栋信息
     */
    public Building getBuildingInfo(Integer buildingId) {
        try {
            return dormMapper.findBuildingById(buildingId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取房间信息
     */
    public Room getRoomInfo(Integer roomId) {
        try {
            return dormMapper.findRoomById(roomId);
        } catch (Exception e) {
            return null;
        }
    }

    public String getNextAvailableBedNo(Integer roomId, Integer capacity) {
        try {
            // 获取房间的所有分配记录
            List<AllocationRecord> allocations = dormMapper.findRoomAllocations(roomId);

            // 获取已占用的床位号
            Set<String> occupiedBeds = new HashSet<>();
            for (AllocationRecord ar : allocations) {
                if (ar.getBedNo() != null && "active".equals(ar.getStatus())) {
                    occupiedBeds.add(ar.getBedNo());
                }
            }

            // 根据房间容量生成标准的床位号顺序
            List<String> standardBeds = generateStandardBedNos(capacity);

            // 查找第一个未占用的床位
            for (String bedNo : standardBeds) {
                if (!occupiedBeds.contains(bedNo)) {
                    return bedNo;
                }
            }

            return null; // 没有可用床位
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成标准床位号（如4人间：A01, B01, A02, B02）
     */
    private List<String> generateStandardBedNos(Integer capacity) {
        List<String> bedNos = new ArrayList<>();

        if (capacity == 2) {
            bedNos.add("A01");
            bedNos.add("B01");
        } else if (capacity == 4) {
            bedNos.add("A01");
            bedNos.add("B01");
            bedNos.add("A02");
            bedNos.add("B02");
        } else if (capacity == 6) {
            bedNos.add("A01");
            bedNos.add("B01");
            bedNos.add("A02");
            bedNos.add("B02");
            bedNos.add("A03");
            bedNos.add("B03");
        } else {
            // 通用规则：每行2个床位，A列和B列
            int rows = (capacity + 1) / 2; // 向上取整
            for (int i = 1; i <= rows; i++) {
                String row = String.format("%02d", i);
                bedNos.add("A" + row);
                if (bedNos.size() < capacity) {
                    bedNos.add("B" + row);
                }
            }
        }

        return bedNos;
    }
}