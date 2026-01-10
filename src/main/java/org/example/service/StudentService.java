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
     * 获取学生主页数据 - 修正字段引用
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
                dormInfo.put("roomNumber", allocation.getRoom().getRoomNo());  // 修改：getRoomNo()
                dormInfo.put("floor", allocation.getRoom().getFloor());
                dormInfo.put("bedNumber", allocation.getBedNo());  // 修改：getBedNo()
                
                // 处理费用：年费转换为月费
                BigDecimal yearlyFee = allocation.getRoom().getYearlyFee();
                if (yearlyFee != null) {
                    // 年费除以12得到月费（保留2位小数）
                    BigDecimal monthlyFee = yearlyFee.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
                    dormInfo.put("monthlyFee", monthlyFee);
                } else {
                    dormInfo.put("monthlyFee", BigDecimal.ZERO);
                }
                
                dormInfo.put("roomType", allocation.getRoom().getGenderType());  // 性别类型作为房间类型
                
                // 获取楼栋名称（从查询结果中直接获取）
                if (allocation instanceof Map) {
                    // 如果MyBatis返回了额外的building_name字段
                    Map<?, ?> allocationMap = (Map<?, ?>) allocation;
                    Object buildingName = allocationMap.get("buildingName");
                    dormInfo.put("buildingName", buildingName != null ? buildingName : "未知楼栋");
                } else {
                    dormInfo.put("buildingName", "未知楼栋");
                }
                
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
    
    /**
     * 验证学生权限
     */
    public boolean validateStudentAccess(Integer userId, Integer studentId) {
        try {
            // 获取用户信息
            User user = userMapper.findById(userId);
            if (user == null || !"student".equals(user.getRole())) {
                return false;
            }
            
            // 验证学生ID对应的用户ID是否匹配
            Integer actualUserId = studentMapper.findUserIdByStudentId(studentId);
            return actualUserId != null && actualUserId.equals(userId);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 检查学生是否有宿舍分配
     */
    public boolean hasDormAllocation(Integer studentId) {
        try {
            AllocationRecord allocation = studentMapper.findCurrentDormAllocation(studentId);
            return allocation != null && allocation.isActive();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取学生的宿舍历史记录
     */
    public List<AllocationRecord> getDormHistory(Integer studentId) {
        try {
            return studentMapper.findDormHistory(studentId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}