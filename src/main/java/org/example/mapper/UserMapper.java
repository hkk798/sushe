package org.example.mapper;

import org.example.entity.User;
import org.example.entity.Student;
import org.example.entity.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    
    // === 用户基本操作 ===
    
    // 根据用户名和密码查找用户
    @Select("SELECT * FROM User WHERE username = #{username} AND password = #{password}")
    User findByUsernameAndPassword(@Param("username") String username, 
                                  @Param("password") String password);
    
    // 根据用户名查找用户
    @Select("SELECT * FROM User WHERE username = #{username}")
    User findByUsername(@Param("username") String username);
    
    // 根据用户ID查找用户
    @Select("SELECT * FROM User WHERE user_id = #{userId}")
    User findById(@Param("userId") Integer userId);
    
    // 插入新用户
    @Insert("INSERT INTO User(username, password, role, real_name, email, phone) " +
            "VALUES(#{username}, #{password}, #{role}, #{realName}, #{email}, #{phone})")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int save(User user);
    
    // 更新密码
    @Update("UPDATE User SET password = #{newPassword} WHERE user_id = #{userId}")
    int updatePassword(@Param("userId") Integer userId, 
                      @Param("newPassword") String newPassword);
    
    // 更新用户状态
    @Update("UPDATE User SET status = #{status} WHERE user_id = #{userId}")
    int updateStatus(@Param("userId") Integer userId, 
                    @Param("status") String status);
    
    // 检查用户名是否存在
    @Select("SELECT COUNT(*) FROM User WHERE username = #{username}")
    int countByUsername(@Param("username") String username);
    
    // 获取所有用户
    @Select("SELECT * FROM User ORDER BY created_at DESC")
    List<User> findAll();
    
    // 根据角色获取用户
    @Select("SELECT * FROM User WHERE role = #{role}")
    List<User> findByRole(@Param("role") String role);
    
    // === 学生相关操作 ===
    
    // 根据学生ID查找学生（带用户信息）
    @Select("SELECT s.*, u.* FROM Student s JOIN User u ON s.user_id = u.user_id WHERE s.student_id = #{studentId}")
    @Results({
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "studentNo", column = "student_no"),
        @Result(property = "major", column = "major"),
        @Result(property = "className", column = "class_name"),
        @Result(property = "gender", column = "gender"),
        @Result(property = "user.userId", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.password", column = "password"),
        @Result(property = "user.role", column = "role"),
        @Result(property = "user.realName", column = "real_name"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "user.phone", column = "phone"),
        @Result(property = "user.status", column = "status"),
        @Result(property = "user.createdAt", column = "created_at")
    })
    Student findStudentById(@Param("studentId") Integer studentId);
    
    // 根据学号查找学生
    @Select("SELECT s.*, u.* FROM Student s JOIN User u ON s.user_id = u.user_id WHERE s.student_no = #{studentNo}")
    @Results({
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "studentNo", column = "student_no"),
        @Result(property = "major", column = "major"),
        @Result(property = "className", column = "class_name"),
        @Result(property = "gender", column = "gender"),
        @Result(property = "user.userId", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.password", column = "password"),
        @Result(property = "user.role", column = "role"),
        @Result(property = "user.realName", column = "real_name"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "user.phone", column = "phone"),
        @Result(property = "user.status", column = "status")
    })
    Student findStudentByStudentNo(@Param("studentNo") String studentNo);
    
    // 根据用户ID查找学生
    // 根据用户ID查找学生 (修改后：关联查询User表，防止登录后出现空指针)
    @Select("SELECT s.*, u.* FROM Student s JOIN User u ON s.user_id = u.user_id WHERE s.user_id = #{userId}")
    @Results({
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "studentNo", column = "student_no"),
            @Result(property = "major", column = "major"),
            @Result(property = "className", column = "class_name"),
            @Result(property = "gender", column = "gender"),
            // 映射 User 对象的信息
            @Result(property = "user.userId", column = "user_id"),
            @Result(property = "user.username", column = "username"),
            @Result(property = "user.password", column = "password"),
            @Result(property = "user.role", column = "role"),
            @Result(property = "user.realName", column = "real_name"),
            @Result(property = "user.email", column = "email"),
            @Result(property = "user.phone", column = "phone"),
            @Result(property = "user.status", column = "status")
    })
    Student findStudentByUserId(@Param("userId") Integer userId);
    
    // 插入学生信息
    @Insert("INSERT INTO Student(user_id, student_no, major, class_name, gender) " +
            "VALUES(#{userId}, #{studentNo}, #{major}, #{className}, #{gender})")
    int saveStudent(Student student);
    
    // 获取所有学生
    @Select("SELECT s.*, u.* FROM Student s JOIN User u ON s.user_id = u.user_id")
    @Results({
        @Result(property = "studentId", column = "student_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "studentNo", column = "student_no"),
        @Result(property = "major", column = "major"),
        @Result(property = "className", column = "class_name"),
        @Result(property = "gender", column = "gender"),
        @Result(property = "user.userId", column = "user_id"),
        @Result(property = "user.realName", column = "real_name"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "user.phone", column = "phone")
    })
    List<Student> findAllStudents();
    
    // === 管理员相关操作 ===
    
    // 根据管理员ID查找管理员（带用户信息）
    @Select("SELECT a.*, u.* FROM Admin a JOIN User u ON a.user_id = u.user_id WHERE a.admin_id = #{adminId}")
    @Results({
        @Result(property = "adminId", column = "admin_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "adminNo", column = "admin_no"),
        @Result(property = "position", column = "position"),
        @Result(property = "workPhone", column = "work_phone"),
        @Result(property = "manageBuilding", column = "manage_building"),
        @Result(property = "user.userId", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.password", column = "password"),
        @Result(property = "user.role", column = "role"),
        @Result(property = "user.realName", column = "real_name"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "user.phone", column = "phone"),
        @Result(property = "user.status", column = "status")
    })
    Admin findAdminById(@Param("adminId") Integer adminId);
    
    // 根据工号查找管理员
    @Select("SELECT a.*, u.* FROM Admin a JOIN User u ON a.user_id = u.user_id WHERE a.admin_no = #{adminNo}")
    @Results({
        @Result(property = "adminId", column = "admin_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "adminNo", column = "admin_no"),
        @Result(property = "position", column = "position"),
        @Result(property = "workPhone", column = "work_phone"),
        @Result(property = "manageBuilding", column = "manage_building"),
        @Result(property = "user.userId", column = "user_id"),
        @Result(property = "user.username", column = "username"),
        @Result(property = "user.password", column = "password"),
        @Result(property = "user.role", column = "role"),
        @Result(property = "user.realName", column = "real_name"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "user.phone", column = "phone")
    })
    Admin findAdminByAdminNo(@Param("adminNo") String adminNo);
    
    // 根据用户ID查找管理员
    @Select("SELECT a.* FROM Admin a WHERE a.user_id = #{userId}")
    Admin findAdminByUserId(@Param("userId") Integer userId);
    
    // 插入管理员信息
    @Insert("INSERT INTO Admin(user_id, admin_no, position, work_phone, manage_building) " +
            "VALUES(#{userId}, #{adminNo}, #{position}, #{workPhone}, #{manageBuilding})")
    int saveAdmin(Admin admin);
    
    // 获取所有管理员
    @Select("SELECT a.*, u.* FROM Admin a JOIN User u ON a.user_id = u.user_id")
    @Results({
        @Result(property = "adminId", column = "admin_id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "adminNo", column = "admin_no"),
        @Result(property = "position", column = "position"),
        @Result(property = "workPhone", column = "work_phone"),
        @Result(property = "manageBuilding", column = "manage_building"),
        @Result(property = "user.userId", column = "user_id"),
        @Result(property = "user.realName", column = "real_name"),
        @Result(property = "user.email", column = "email"),
        @Result(property = "user.phone", column = "phone")
    })
    List<Admin> findAllAdmins();
    
    // 根据类型获取管理员
    @Select("SELECT a.*, u.* FROM Admin a JOIN User u ON a.user_id = u.user_id WHERE u.role = #{role}")
    @Results({
            @Result(property = "adminId", column = "admin_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "adminNo", column = "admin_no"),
            // @Result(property = "adminType", column = "admin_type"), // [已删除]
            @Result(property = "position", column = "position"),
            @Result(property = "workPhone", column = "work_phone"),
            @Result(property = "manageBuilding", column = "manage_building"),
            @Result(property = "user.userId", column = "user_id"),
            @Result(property = "user.realName", column = "real_name")
    })
    List<Admin> findAdminsByRole(@Param("role") String role);


    // 在 UserMapper 接口中添加
    @Select("SELECT COUNT(*) FROM User")
    int countAll();

    @Select("<script>" +
            "SELECT u.*, s.student_no, s.major, s.class_name " +  // <--- 修改点1：查出 student_no
            "FROM User u " +
            "LEFT JOIN Student s ON u.user_id = s.user_id " +
            "WHERE 1=1 " +
            "<if test='role != null and role != \"\"'> AND u.role = #{role} </if> " +
            "<if test='studentNo != null and studentNo != \"\"'> AND s.student_no LIKE CONCAT('%', #{studentNo}, '%') </if> " + // <--- 修改点2：增加筛选条件
            "<if test='major != null and major != \"\"'> AND s.major LIKE CONCAT('%', #{major}, '%') </if> " +
            "<if test='className != null and className != \"\"'> AND s.class_name LIKE CONCAT('%', #{className}, '%') </if> " +
            "ORDER BY u.created_at DESC" +
            "</script>")
    List<User> searchUsers(@Param("role") String role,
                           @Param("studentNo") String studentNo, // [新增参数]
                           @Param("major") String major,
                           @Param("className") String className);
}