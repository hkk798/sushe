package org.example.mapper;

import org.example.entity.RepairOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RepairMapper {
    
    /**
     * 保存报修单（适配您的表结构）
     */
    @Insert("INSERT INTO RepairOrder(student_id, room_id, title, category, description, " +
            "images, submit_time, status) " +
            "VALUES(#{studentId}, #{roomId}, #{title}, #{category}, #{description}, " +
            "#{images}, NOW(), #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int saveRepairOrder(RepairOrder repairOrder);
    
    /**
     * 根据学生ID查找报修历史（简化版，不查询关联表）
     */
    @Select("SELECT order_id, student_id, room_id, title, description, category, " +
            "images, submit_time, status, processor_id, process_time, process_result " +
            "FROM RepairOrder " +
            "WHERE student_id = #{studentId} " +
            "ORDER BY submit_time DESC")
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "roomId", column = "room_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "processorId", column = "processor_id"),
        @Result(property = "processTime", column = "process_time"),
        @Result(property = "processResult", column = "process_result")
    })
    List<RepairOrder> findRepairHistoryByStudentId(@Param("studentId") Integer studentId);
    
    /**
     * 根据学生ID和状态筛选报修历史（简化版）
     */
    @Select("<script>" +
            "SELECT order_id, student_id, room_id, title, description, category, " +
            "images, submit_time, status, processor_id, process_time, process_result " +
            "FROM RepairOrder " +
            "WHERE student_id = #{studentId} " +
            "<if test='status != \"all\"'>" +
            "   AND status = #{status} " +
            "</if>" +
            "ORDER BY submit_time DESC" +
            "</script>")
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "roomId", column = "room_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "processorId", column = "processor_id"),
        @Result(property = "processTime", column = "process_time"),
        @Result(property = "processResult", column = "process_result")
    })
    List<RepairOrder> findRepairHistoryByStudentIdAndStatus(@Param("studentId") Integer studentId,
                                                           @Param("status") String status);
    
    /**
     * 根据房间ID查找报修历史（简化版）
     */
    @Select("SELECT order_id, student_id, room_id, title, description, category, " +
            "images, submit_time, status, processor_id, process_time, process_result " +
            "FROM RepairOrder " +
            "WHERE room_id = #{roomId} " +
            "ORDER BY submit_time DESC")
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "roomId", column = "room_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "processorId", column = "processor_id"),
        @Result(property = "processTime", column = "process_time"),
        @Result(property = "processResult", column = "process_result")
    })
    List<RepairOrder> findRepairHistoryByRoomId(@Param("roomId") Integer roomId);
    
    /**
     * 统计学生待处理的报修单数量
     */
    @Select("SELECT COUNT(*) FROM RepairOrder " +
            "WHERE student_id = #{studentId} AND status = 'pending'")
    int countPendingRepairsByStudentId(@Param("studentId") Integer studentId);
    
    /**
     * 更新报修单状态
     */
    @Update("UPDATE RepairOrder SET status = #{status}, " +
            "processor_id = #{processorId}, process_time = NOW(), " +
            "process_result = #{processResult} " +
            "WHERE order_id = #{orderId}")
    int updateRepairStatus(@Param("orderId") Integer orderId,
                          @Param("status") String status,
                          @Param("processorId") Integer processorId,
                          @Param("processResult") String processResult);
    
    /**
     * 根据ID查找报修单（基础信息版，不查关联表）
     */
    @Select("SELECT order_id, student_id, room_id, title, description, category, " +
            "images, submit_time, status, processor_id, process_time, process_result " +
            "FROM RepairOrder " +
            "WHERE order_id = #{orderId}")
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "roomId", column = "room_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "processorId", column = "processor_id"),
        @Result(property = "processTime", column = "process_time"),
        @Result(property = "processResult", column = "process_result")
    })
    RepairOrder findRepairOrderById(@Param("orderId") Integer orderId);
    
    /**
     * 添加评价和反馈
     */
    @Update("UPDATE RepairOrder SET rating = #{rating}, feedback = #{feedback} " +
            "WHERE order_id = #{orderId}")
    int addRatingAndFeedback(@Param("orderId") Integer orderId,
                            @Param("rating") Integer rating,
                            @Param("feedback") String feedback);
    
    /**
     * 删除报修单
     */
    @Delete("DELETE FROM RepairOrder WHERE order_id = #{orderId}")
    int deleteRepairOrder(@Param("orderId") Integer orderId);
    
    /**
     * 统计各状态的报修单数量
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN status = 'pending' THEN 1 END) as pending_count, " +
            "COUNT(CASE WHEN status = 'processing' THEN 1 END) as processing_count, " +
            "COUNT(CASE WHEN status = 'completed' THEN 1 END) as completed_count, " +
            "COUNT(*) as total_count " +
            "FROM RepairOrder " +
            "WHERE student_id = #{studentId}")
    Map<String, Object> countRepairStatusByStudentId(@Param("studentId") Integer studentId);
    
    /**
     * 根据ID查找报修单（最简版）
     */
    @Select("SELECT * FROM RepairOrder WHERE order_id = #{orderId}")
    @Results({
        @Result(property = "orderId", column = "order_id"),
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "roomId", column = "room_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "category", column = "category"),
        @Result(property = "images", column = "images"),
        @Result(property = "submitTime", column = "submit_time"),
        @Result(property = "status", column = "status"),
        @Result(property = "processorId", column = "processor_id"),
        @Result(property = "processTime", column = "process_time"),
        @Result(property = "processResult", column = "process_result")
    })
    RepairOrder findRepairOrderSimpleById(@Param("orderId") Integer orderId);




    @Select("<script>" +
            "SELECT ro.*, r.room_no, b.building_no, s.student_no, u.real_name as studentName " +
            "FROM RepairOrder ro " +
            "JOIN Room r ON ro.room_id = r.room_id " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "JOIN Student s ON ro.student_id = s.student_id " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE 1=1 " +
            "<if test='status != null and status != \"all\"'>" +
            "  AND ro.status = #{status} " +
            "</if>" +
            "AND b.building_no IN " +
            "<foreach item='item' collection='buildingNos' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "ORDER BY ro.submit_time ASC" +
            "</script>")
    @Results({
            @Result(property = "order_id", column = "order_id"), // 映射 Map 的 key
            @Result(property = "room_no", column = "room_no"),
            @Result(property = "building_no", column = "building_no"),
            @Result(property = "studentName", column = "studentName"),
            // 其他需要的字段可以自动映射到 Map
    })
    List<Map<String, Object>> findByBuildingNosAndStatus(@Param("buildingNos") List<String> buildingNos,
                                                         @Param("status") String status);

    /**
     * [新增] 更新报修状态和处理人信息
     */
    @Update("UPDATE RepairOrder SET status = #{status}, processor_id = #{adminId}, " +
            "process_time = NOW() " +
            "WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") Integer orderId,
                     @Param("status") String status,
                     @Param("adminId") Integer adminId);
}