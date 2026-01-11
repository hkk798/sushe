package org.example.mapper;

import org.apache.ibatis.annotations.*;
import org.example.entity.Student;
import org.example.entity.AllocationRecord;

import java.util.List;
import java.util.Map;

@Mapper
public interface StudentMapper {

    // 1. 根据ID查询学生 (AllocationService 必须用到这个！)
    // 我们做一个联表查询，顺便把 User 表里的 real_name 查出来
    @Select("SELECT s.*, u.real_name " +
            "FROM Student s " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE s.student_id = #{id}")
    Student findById(Integer id);

    // 2. 查询所有学生 (以后用)
    @Select("SELECT s.*, u.real_name " +
            "FROM Student s " +
            "JOIN User u ON s.user_id = u.user_id")
    List<Student> findAll();



    // 根据学生ID获取完整学生信息（关联User表）
    @Select("SELECT s.*, u.real_name, u.email, u.phone, u.status " +
            "FROM Student s " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE s.student_id = #{studentId}")
    @Results({
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "major", column = "major"),
            @Result(property = "className", column = "class_name"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "user.realName", column = "real_name"),
            @Result(property = "user.email", column = "email"),
            @Result(property = "user.phone", column = "phone"),
            @Result(property = "user.status", column = "status")
    })
    Student findStudentWithUserInfo(@Param("studentId") Integer studentId);


    // 获取学生的宿舍分配信息 - 修改：使用正确的字段名
    @Select("SELECT ar.*, r.room_no, r.floor, r.building_id, r.capacity, r.current_count, " +
            "r.yearly_fee, r.gender_type, r.status as room_status, b.building_name " +
            "FROM AllocationRecord ar " +
            "JOIN Room r ON ar.room_id = r.room_id " +
            "LEFT JOIN Building b ON r.building_id = b.building_id " +
            "WHERE ar.student_id = #{studentId} " +
            "AND ar.status = 'active' " +
            "AND (ar.check_out_date IS NULL OR ar.check_out_date > NOW()) " +
            "ORDER BY ar.allocate_date DESC " +
            "LIMIT 1")
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
            @Result(property = "bedNo", column = "bed_no"),  // 注意：这里是bed_no
            @Result(property = "remarks", column = "remarks"),
            // 关联的Room对象
            @Result(property = "room.roomId", column = "room_id"),
            @Result(property = "room.buildingId", column = "building_id"),
            @Result(property = "room.roomNo", column = "room_no"),  // 注意：这里是room_no
            @Result(property = "room.floor", column = "floor"),
            @Result(property = "room.capacity", column = "capacity"),
            @Result(property = "room.currentCount", column = "current_count"),
            @Result(property = "room.yearlyFee", column = "yearly_fee"),  // 注意：这里是yearly_fee
            @Result(property = "room.genderType", column = "gender_type"),
            @Result(property = "room.status", column = "room_status"),
            // 额外的楼栋名称
            @Result(property = "buildingName", column = "building_name")
    })
    AllocationRecord findCurrentDormAllocation(@Param("studentId") Integer studentId);



    // 获取学生的舍友信息 - 修改：使用正确的字段名
    @Select("SELECT s.student_id, s.student_no, u.real_name, ar.bed_no " +
            "FROM Student s " +
            "JOIN User u ON s.user_id = u.user_id " +
            "JOIN AllocationRecord ar ON s.student_id = ar.student_id " +
            "WHERE ar.room_id = #{roomId} " +
            "AND ar.status = 'active' " +
            "AND s.student_id != #{excludeStudentId} " +
            "ORDER BY ar.bed_no")
    @Results({
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "bedNo", column = "bed_no")
    })
    List<Map<String, Object>> findRoommates(@Param("roomId") Integer roomId,
                                            @Param("excludeStudentId") Integer excludeStudentId);


    // 统计学生处理中的报修单数量
    @Select("SELECT COUNT(*) FROM RepairOrder " +
            "WHERE student_id = #{studentId} " +
            "AND status = 'processing' " +
            "AND (processor_id IS NOT NULL)")
    Integer countProcessingRepairs(@Param("studentId") Integer studentId);

    // 根据学生ID获取用户ID
    @Select("SELECT user_id FROM Student WHERE student_id = #{studentId}")
    Integer findUserIdByStudentId(@Param("studentId") Integer studentId);

    // 获取学生的所有宿舍分配历史
    @Select("SELECT ar.*, r.room_no, b.building_name " +
            "FROM AllocationRecord ar " +
            "JOIN Room r ON ar.room_id = r.room_id " +
            "LEFT JOIN Building b ON r.building_id = b.building_id " +
            "WHERE ar.student_id = #{studentId} " +
            "ORDER BY ar.allocate_date DESC")
    @Results({
            @Result(property = "recordId", column = "record_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "roomId", column = "room_id"),
            @Result(property = "allocateDate", column = "allocate_date"),
            @Result(property = "checkInDate", column = "check_in_date"),
            @Result(property = "checkOutDate", column = "check_out_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "bedNo", column = "bed_no"),
            @Result(property = "room.roomNo", column = "room_no"),
            @Result(property = "buildingName", column = "building_name")
    })
    List<AllocationRecord> findDormHistory(@Param("studentId") Integer studentId);



    @Select("SELECT COUNT(*) FROM Student s " +
            "WHERE NOT EXISTS (SELECT 1 FROM AllocationRecord ar WHERE ar.student_id = s.student_id AND ar.status = 'active')")
    int countUnassignedStudents();
}


