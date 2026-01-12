package org.example.mapper;

import org.example.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface DormMapper {

    // 根据楼栋ID查找楼栋信息
    @Select("SELECT * FROM Building WHERE building_id = #{buildingId}")
    @Results({
            @Result(property = "buildingId", column = "building_id"),
            @Result(property = "buildingNo", column = "building_no"),
            @Result(property = "buildingName", column = "building_name"),
            @Result(property = "roomCount", column = "room_count"),
            @Result(property = "genderType", column = "gender_type"),
            @Result(property = "builtYear", column = "built_year"),
            @Result(property = "status", column = "status"),
            @Result(property = "address", column = "address")
    })
    Building findBuildingById(@Param("buildingId") Integer buildingId);

    // 根据房间ID查找房间信息
    @Select("SELECT * FROM Room WHERE room_id = #{roomId}")
    @Results({
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "buildingId", column = "building_id"),
            @Result(property = "roomNo", column = "room_no"),
            @Result(property = "capacity", column = "capacity"),
            @Result(property = "currentCount", column = "current_count"),
            @Result(property = "genderType", column = "gender_type"),
            @Result(property = "status", column = "status"),
            @Result(property = "yearlyFee", column = "yearly_fee"),
            @Result(property = "floor", column = "floor")
    })
    Room findRoomById(@Param("roomId") Integer roomId);

    // 获取房间的所有分配记录（包括舍友的详细信息）
    @Select("SELECT ar.*, s.*, u.real_name, u.email, u.phone " +
            "FROM AllocationRecord ar " +
            "JOIN Student s ON ar.student_id = s.student_id " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE ar.room_id = #{roomId} " +
            "AND ar.status = 'active' " +
            "ORDER BY ar.bed_no")
    @Results({
            @Result(property = "recordId", column = "record_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "adminId", column = "admin_id"),
            @Result(property = "allocateDate", column = "allocate_date"),
            @Result(property = "checkInDate", column = "check_in_date"),
            @Result(property = "checkOutDate", column = "check_out_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "feeStatus", column = "fee_status"),
            @Result(property = "bedNo", column = "bed_no"),
            @Result(property = "remarks", column = "remarks"),
            // 关联的Student对象
            @Result(property = "student.studentId", column = "student_id"),
            @Result(property = "student.userId", column = "user_id"),
            @Result(property = "student.studentNo", column = "student_no"),
            @Result(property = "student.major", column = "major"),
            @Result(property = "student.className", column = "class_name"),
            @Result(property = "student.gender", column = "gender"),
            // 关联的User对象（通过Student）
            @Result(property = "student.user.realName", column = "real_name"),
            @Result(property = "student.user.email", column = "email"),
            @Result(property = "student.user.phone", column = "phone")
    })
    List<AllocationRecord> findRoomAllocations(@Param("roomId") Integer roomId);

    // 获取房间的报修历史
    @Select("SELECT ro.*, s.student_no, u.real_name as student_name, " +
            "a.admin_no, au.real_name as processor_name " +
            "FROM RepairOrder ro " +
            "LEFT JOIN Student s ON ro.student_id = s.student_id " +
            "LEFT JOIN User u ON s.user_id = u.user_id " +
            "LEFT JOIN Admin a ON ro.processor_id = a.admin_id " +
            "LEFT JOIN User au ON a.user_id = au.user_id " +
            "WHERE ro.room_id = #{roomId} " +
            "ORDER BY ro.submit_time DESC")
    @Results({
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "category", column = "category"),
            @Result(property = "submitTime", column = "submit_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "processorId", column = "processor_id"),
            @Result(property = "processTime", column = "process_time"),
            @Result(property = "processResult", column = "process_result"),
            // 关联的Student信息
            @Result(property = "student.studentNo", column = "student_no"),
            @Result(property = "student.user.realName", column = "student_name"),
            // 关联的Admin信息
            @Result(property = "processor.adminNo", column = "admin_no"),
            @Result(property = "processor.user.realName", column = "processor_name")
    })
    List<RepairOrder> findRoomRepairHistory(@Param("roomId") Integer roomId);

    // 获取楼栋中的所有可用房间
    @Select("SELECT * FROM Room " +
            "WHERE building_id = #{buildingId} " +
            "AND status = 'available' " +
            "AND current_count < capacity " +
            "ORDER BY floor, room_no")
    @Results({
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "buildingId", column = "building_id"),
            @Result(property = "roomNo", column = "room_no"),
            @Result(property = "capacity", column = "capacity"),
            @Result(property = "currentCount", column = "current_count"),
            @Result(property = "genderType", column = "gender_type"),
            @Result(property = "status", column = "status"),
            @Result(property = "yearlyFee", column = "yearly_fee"),
            @Result(property = "floor", column = "floor")
    })
    List<Room> findAvailableRooms(@Param("buildingId") Integer buildingId);

    // 统计楼栋的入住情况
    @Select("SELECT COUNT(*) as totalRooms, " +
            "SUM(capacity) as totalCapacity, " +
            "SUM(current_count) as totalOccupied " +
            "FROM Room " +
            "WHERE building_id = #{buildingId}")
    Map<String, Object> getBuildingStatistics(@Param("buildingId") Integer buildingId);
}