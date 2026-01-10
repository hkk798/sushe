package org.example.mapper;

import org.example.entity.Student;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

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
}