package org.example.mapper;

import org.example.entity.AllocationRecord;
import org.example.entity.Room;
import org.example.entity.Student;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AllocationRecordMapper {

    // 1. 查询未分配的学生
    // 逻辑：在 Student 表中，但不在 'active' 状态的 AllocationRecord 表中
    @Select("SELECT s.*, u.real_name " +
            "FROM Student s " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE s.student_id NOT IN (SELECT student_id FROM AllocationRecord WHERE status = 'active')")
    List<Student> findUnallocatedStudents();

    // 2. 根据性别查询可用房间
    // 逻辑：status='available' 且 gender_type 符合
    @Select("SELECT r.*, b.building_name " +
            "FROM Room r " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "WHERE r.gender_type = #{gender} " +
            "AND r.status = 'available' " +
            "ORDER BY b.building_name, r.room_no")
    List<Room> findAvailableRoomsByGender(String gender);

    // 3. 插入分配记录
    // 注意：您的SQL里定义了触发器，插入后数据库会自动更新 Room 的 current_count，我们不用管
    @Insert("INSERT INTO AllocationRecord(student_id, room_id, admin_id, allocate_date, status, fee_status) " +
            "VALUES(#{studentId}, #{roomId}, #{adminId}, #{allocateDate}, 'active', 'unpaid')")
    @Options(useGeneratedKeys = true, keyProperty = "recordId")
    int insert(AllocationRecord record);
}