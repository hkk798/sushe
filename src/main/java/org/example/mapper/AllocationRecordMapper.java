package org.example.mapper;

import org.example.entity.AllocationRecord;
import org.example.entity.Room;
import org.example.entity.Student;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AllocationRecordMapper {

    // 查询未分配的学生
    @Select("SELECT s.*, u.real_name " +
            "FROM Student s " +
            "JOIN User u ON s.user_id = u.user_id " +
            "WHERE s.student_id NOT IN (SELECT student_id FROM AllocationRecord WHERE status = 'active')")
    List<Student> findUnallocatedStudents();

    // 根据性别查询可用房间
    @Select("SELECT r.*, b.building_name " +
            "FROM Room r " +
            "JOIN Building b ON r.building_id = b.building_id " +
            "WHERE r.gender_type = #{gender} " +
            "AND r.status = 'available' " +
            "ORDER BY b.building_name, r.room_no")
    List<Room> findAvailableRoomsByGender(String gender);

    // 插入分配记录
    @Insert("INSERT INTO AllocationRecord(student_id, room_id, admin_id, allocate_date, status, fee_status) " +
            "VALUES(#{studentId}, #{roomId}, #{adminId}, #{allocateDate}, 'active', 'unpaid')")
    @Options(useGeneratedKeys = true, keyProperty = "recordId")
    int insert(AllocationRecord record);

    // ✅ [新增] 结束当前的入住记录（用于换寝室）
    @Update("UPDATE AllocationRecord SET status = 'completed', check_out_date = NOW() " +
            "WHERE student_id = #{studentId} AND status = 'active'")
    int completeActiveRecord(Integer studentId);
}