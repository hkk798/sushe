package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.entity.RepairOrder;

import java.util.List;
import java.util.Map;

@Mapper
public interface RepairOrderMapper {

    // 统计所有报修单数量
    @Select("SELECT COUNT(*) FROM RepairOrder")
    int countAll();

    /**
     * [新增] 统计指定楼栋未完成（待处理 + 处理中）的报修单数量
     * 用于宿管工作台统计卡片
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM RepairOrder ro " +
            "JOIN Room r ON ro.room_id = r.room_id " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "WHERE ro.status != 'completed' " +  // 修改点：只要不是已完成的都纳入统计
            "AND b.building_no IN " +
            "<foreach item='item' collection='buildingNos' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    int countUnfinishedByBuildingNos(@Param("buildingNos") List<String> buildingNos);

    /**
     * [保留] 统计待处理 (Status='pending')
     * (保留此方法以防其他地方使用)
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM RepairOrder ro " +
            "JOIN Room r ON ro.room_id = r.room_id " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "WHERE ro.status = 'pending' " +
            "AND b.building_no IN " +
            "<foreach item='item' collection='buildingNos' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    int countPendingByBuildingNos(@Param("buildingNos") List<String> buildingNos);

    /**
     * [新增] 获取指定楼栋最新的未完成报修单 (前5条)
     * 用于宿管工作台"最新未完成报修"列表
     */
    @Select("<script>" +
            "SELECT ro.*, r.room_no, b.building_no, u.real_name as studentName " +
            "FROM RepairOrder ro " +
            "JOIN Room r ON ro.room_id = r.room_id " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "JOIN Student s ON ro.student_id = s.student_id " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE ro.status != 'completed' " + // 修改点：显示所有未完成的任务
            "AND b.building_no IN " +
            "<foreach item='item' collection='buildingNos' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "ORDER BY ro.submit_time DESC LIMIT 5" +
            "</script>")
    List<Map<String, Object>> findLatestUnfinishedByBuildingNos(@Param("buildingNos") List<String> buildingNos);

    /**
     * [保留] 原有的查询最新待处理方法
     */
    @Select("<script>" +
            "SELECT ro.*, r.room_no, b.building_no, u.real_name as studentName " +
            "FROM RepairOrder ro " +
            "JOIN Room r ON ro.room_id = r.room_id " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "JOIN Student s ON ro.student_id = s.student_id " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE ro.status = 'pending' " +
            "AND b.building_no IN " +
            "<foreach item='item' collection='buildingNos' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "ORDER BY ro.submit_time DESC LIMIT 5" +
            "</script>")
    List<Map<String, Object>> findLatestPendingByBuildingNos(@Param("buildingNos") List<String> buildingNos);

    // 根据楼栋编号列表和状态查询报修单 (报修管理列表页使用)
    @Select("<script>" +
            "SELECT ro.*, r.room_no, b.building_no, s.student_no, s.student_id, u.real_name as studentName " +
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
    List<Map<String, Object>> findByBuildingNosAndStatus(@Param("buildingNos") List<String> buildingNos,
                                                         @Param("status") String status);

    /**
     * 更新报修状态、处理人信息和处理结果
     */
    @Update("UPDATE RepairOrder SET status = #{status}, processor_id = #{adminId}, " +
            "process_result = #{processResult}, " +
            "process_time = NOW() " +
            "WHERE order_id = #{orderId}")
    int updateStatus(@Param("orderId") Integer orderId,
                     @Param("status") String status,
                     @Param("adminId") Integer adminId,
                     @Param("processResult") String processResult);
}