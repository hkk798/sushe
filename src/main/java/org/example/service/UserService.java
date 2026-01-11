package org.example.service;

import org.example.entity.User;
import org.example.entity.Student;
import org.example.entity.Admin;
import org.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.example.vo.UserImportVO; // 新增


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 用户登录验证
     */
    public Map<String, Object> login(String username, String password, String role) {
        Map<String, Object> result = new HashMap<>();
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(role)) {
            return null;
        }
        
        User user = null;

        // 根据角色采用不同的查找策略
        if ("student".equals(role)) {
            // 对于学生：使用学号查找对应的学生，再获取其用户信息
            Student student = userMapper.findStudentByStudentNo(username); // username实际上是学号
            if (student != null && student.getUser() != null) {
                user = student.getUser();
                // 验证密码
                if (!password.equals(user.getPassword())) {
                    return null;
                }
            }
        } else {
            // 对于管理员：直接使用用户名+密码查找
            Admin admin = userMapper.findAdminByAdminNo(username);
            if (admin != null && admin.getUser() != null) {
                user = admin.getUser();
                if (!password.equals(user.getPassword())) {
                    return null;
                }
            }
        }
        
        if (user == null) {
            return null;
        }
        
        // 验证角色匹配
        if (!role.equals(user.getRole())) {
            return null;
        }
        
        // 验证用户状态
        if (!"active".equals(user.getStatus())) {
            return null;
        }
        
        result.put("user", user);
        
        // 根据角色获取详细信息
        if ("student".equals(role)) {
            Student student = userMapper.findStudentByUserId(user.getUserId());
            result.put("student", student);
        } else if ("building_admin".equals(role) || "system_admin".equals(role)) {
            Admin admin = userMapper.findAdminByUserId(user.getUserId());
            result.put("admin", admin);
        }
        
        return result;
    }
    
    /**
     * 根据用户ID和角色获取完整信息
     */
    public Map<String, Object> getUserWithDetails(Integer userId, String role) {
        Map<String, Object> result = new HashMap<>();
        
        User user = userMapper.findById(userId);
        if (user == null) {
            return null;
        }
        
        result.put("user", user);
        
        // 根据角色获取详细信息
        if ("student".equals(role)) {
            Student student = userMapper.findStudentByUserId(userId);
            result.put("student", student);
        } else if ("building_admin".equals(role) || "system_admin".equals(role)) {
            Admin admin = userMapper.findAdminByUserId(userId);
            result.put("admin", admin);
        }
        
        return result;
    }
    
    /**
     * 注册用户
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user, Student student, Admin admin) { // 方法签名可以抛出异常，或者让运行时异常直接抛出
        if (user == null || !StringUtils.hasText(user.getUsername()) ||
                !StringUtils.hasText(user.getPassword()) || !StringUtils.hasText(user.getRole())) {
            throw new IllegalArgumentException("用户信息不完整"); // 主动抛出异常
        }

        // 检查用户名是否已存在
        if (userMapper.countByUsername(user.getUsername()) > 0) {
            throw new RuntimeException("用户名已存在"); // 这里也可以抛异常，或者返回 false 都可以，因为这里还没写数据库
        }

        // ！！！注意：下面不要用 try-catch 包裹，直接写逻辑！！！

        // 1. 保存用户基本信息
        int result = userMapper.save(user);
        if (result <= 0) {
            throw new RuntimeException("用户基本信息保存失败");
        }

        // 2. 根据角色保存详细信息
        if ("student".equals(user.getRole()) && student != null) {
            student.setUserId(user.getUserId());
            userMapper.saveStudent(student); // 如果这里报错，异常会向上抛出，触发 userMapper.save(user) 的回滚
        } else if (("building_admin".equals(user.getRole()) || "system_admin".equals(user.getRole())) && admin != null) {
            admin.setUserId(user.getUserId());
            userMapper.saveAdmin(admin); // 同上，如果这里报错，自动回滚
        }

        return true;
    }
    
    /**
     * 修改密码
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        if (userId == null || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return false;
        }
        
        // 获取用户信息
        User user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!oldPassword.equals(user.getPassword())) {
            return false;
        }
        
        // 验证新旧密码不能相同
        if (oldPassword.equals(newPassword)) {
            return false;
        }
        
        // 更新密码
        try {
            int result = userMapper.updatePassword(userId, newPassword);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }
    
    /**
     * 获取所有学生
     */
    public List<Student> getAllStudents() {
        return userMapper.findAllStudents();
    }
    
    /**
     * 获取所有管理员
     */
    public List<Admin> getAllAdmins() {
        return userMapper.findAllAdmins();
    }
    
    /**
     * 根据类型获取管理员
     */
    public List<Admin> getAdminsByRole(String role) {
        return userMapper.findAdminsByRole(role);
    }
    
    /**
     * 更新用户状态
     */
    public boolean updateUserStatus(Integer userId, String status) {
        if (userId == null || !StringUtils.hasText(status)) {
            return false;
        }
        
        try {
            int result = userMapper.updateStatus(userId, status);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyUserIdentity(String username, String email, String role) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(email) || !StringUtils.hasText(role)) {
            return false;
        }
        
        try {
            // 根据角色查询用户
            if ("student".equals(role)) {
                // 如果是学生，通过学号查找
                var student = userMapper.findStudentByStudentNo(username);
                if (student != null && student.getUser() != null) {
                    return email.equals(student.getUser().getEmail());
                }
            } else if ("building_admin".equals(role) || "system_admin".equals(role)) {
                // 如果是管理员，通过工号查找
                var admin = userMapper.findAdminByAdminNo(username);
                if (admin != null && admin.getUser() != null) {
                    return email.equals(admin.getUser().getEmail());
                }
            }
            
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 重置密码
     */
    public boolean resetPassword(String username, String newPassword) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(newPassword)) {
            return false;
        }
        
        try {
            // 查找用户
            var user = userMapper.findByUsername(username);
            if (user == null) {
                return false;
            }
            
            // 更新密码
            int result = userMapper.updatePassword(user.getUserId(), newPassword);
            return result > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String validatePasswordChange(String oldPassword, String newPassword, String confirmPassword) {
        // 检查是否填写完整
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword) || 
            !StringUtils.hasText(confirmPassword)) {
            return "请填写所有必填字段";
        }
        
        // 检查新密码长度
        if (newPassword.length() < 6) {
            return "新密码长度不能少于6位";
        }
        
        // 检查两次输入的新密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            return "两次输入的新密码不一致";
        }
        
        // 检查新旧密码是否相同
        if (oldPassword.equals(newPassword)) {
            return "新密码不能与旧密码相同";
        }
        
        return null; // 验证通过
    }

    /**
     * 检查用户名是否存在
     */
    public boolean checkUsernameExists(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }
        
        int count = userMapper.countByUsername(username);
        return count > 0;
    }

    public List<User> searchUsers(String role, String major, String className) {
        return userMapper.searchUsers(role, major, className);
    }

    /**
     * [新增] 批量导入用户
     * @return 返回导入结果信息（例如：成功10条，失败2条）
     */
    @Transactional // 开启事务，虽然我们是逐条插入，但建议保持一致性
    public String importUsers(List<UserImportVO> list) {
        int successCount = 0;
        int failCount = 0;
        StringBuilder failReason = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            UserImportVO vo = list.get(i);
            try {
                // 1. 基础数据校验
                if (vo.getUsername() == null || vo.getPassword() == null || vo.getRole() == null) {
                    throw new RuntimeException("必填项缺失");
                }

                // 2. 构建 User 对象
                User user = new User();
                user.setUsername(vo.getUsername());
                user.setPassword(vo.getPassword());
                user.setRealName(vo.getRealName());
                user.setRole(vo.getRole());
                user.setPhone(vo.getPhone());
                user.setEmail(vo.getEmail());
                user.setStatus("active");

                // 3. 根据角色构建 Student 或 Admin
                Student student = null;
                Admin admin = null;

                if ("student".equals(vo.getRole())) {
                    student = new Student();
                    student.setStudentNo(vo.getStudentNo());
                    student.setMajor(vo.getMajor());
                    student.setClassName(vo.getClassName());
                    student.setGender(vo.getGender() != null ? vo.getGender() : "M");

                    // 简单校验
                    if (student.getStudentNo() == null) throw new RuntimeException("学生缺少学号");

                } else if ("building_admin".equals(vo.getRole()) || "system_admin".equals(vo.getRole())) {
                    admin = new Admin();
                    admin.setAdminNo(vo.getAdminNo());
                    admin.setPosition(vo.getPosition());
                    admin.setWorkPhone(vo.getPhone());

                    // 简单校验
                    if (admin.getAdminNo() == null) throw new RuntimeException("管理员缺少工号");
                }

                // 4. 调用注册方法
                boolean result = register(user, student, admin);
                if (result) {
                    successCount++;
                } else {
                    failCount++;
                    failReason.append("第").append(i + 2).append("行保存失败(可能用户名已存在); ");
                }

            } catch (Exception e) {
                failCount++;
                failReason.append("第").append(i + 2).append("行错误:").append(e.getMessage()).append("; ");
            }
        }

        return "导入完成！成功: " + successCount + " 条, 失败: " + failCount + " 条。 " + (failCount > 0 ? "详情: " + failReason.toString() : "");
    }

}