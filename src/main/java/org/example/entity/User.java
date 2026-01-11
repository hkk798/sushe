package org.example.entity;
import java.util.Date;

/**
 * 用户表实体类
 */
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String role;      
    private String realName;      
    private String email;
    private String phone;
    private String status;       
    private Date createdAt;


    private String major;     // 专业
    private String className; // 班级
    
    // 无参构造
    public User() {
        this.createdAt = new Date();
        this.status = "active";
    }
    
    // 全参构造
    public User(String username, String password, String role, String realName) {
        this();
        this.username = username;
        this.password = password;
        this.role = role;
        this.realName = realName;
    }
    
    // Getter和Setter
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    // 业务方法
    public boolean isStudent() {
        return "student".equals(this.role);
    }
    
    public boolean isBuildingAdmin() {
        return "building_admin".equals(this.role);
    }
    
    public boolean isSystemAdmin() {
        return "system_admin".equals(this.role);
    }
    
    public boolean isActive() {
        return "active".equals(this.status);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", realName='" + realName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }



    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}