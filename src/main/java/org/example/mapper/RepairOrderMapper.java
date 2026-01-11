package org.example.mapper;

import org.example.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface RepairOrderMapper {
    // 统计所有报修单数量
    @Select("SELECT COUNT(*) FROM RepairOrder")
    int countAll();

    // 这里后续还需要添加查询列表等方法

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

    // [新增] 获取指定楼栋最新的待处理报修单 (限制5条)
    @Select("<script>" +
            "SELECT ro.*, r.room_no, b.building_no, s.real_name as studentName " +
            "FROM RepairOrder ro " +
            "JOIN Room r ON ro.room_id = r.room_id " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "JOIN Student s ON ro.student_id = s.student_id " +
            "WHERE ro.status = 'pending' " +
            "AND b.building_no IN " +
            "<foreach item='item' collection='buildingNos' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "ORDER BY ro.submit_time DESC LIMIT 5" +
            "</script>")
    List<Map<String, Object>> findLatestPendingByBuildingNos(@Param("buildingNos") List<String> buildingNos);
}