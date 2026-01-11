package org.example.vo;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 用户导入 VO 类
 * 用于接收 Excel 导入的数据
 */
public class UserImportVO {

    @ExcelProperty("用户名*")
    private String username;

    @ExcelProperty("密码*")
    private String password;

    @ExcelProperty("真实姓名*")
    private String realName;

    @ExcelProperty("角色(student/building_admin/system_admin)*")
    private String role;

    @ExcelProperty("电话")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    // === 学生特有字段 ===
    @ExcelProperty("学号(学生必填)")
    private String studentNo;

    @ExcelProperty("专业(学生必填)")
    private String major;

    @ExcelProperty("班级(学生必填)")
    private String className;

    @ExcelProperty("性别(M/F)")
    private String gender;

    // === 管理员特有字段 ===
    @ExcelProperty("工号(管理员必填)")
    private String adminNo;

    @ExcelProperty("职位")
    private String position;

    // ================== Getters and Setters ==================

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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAdminNo() {
        return adminNo;
    }

    public void setAdminNo(String adminNo) {
        this.adminNo = adminNo;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    // 可选：方便调试打印日志
    @Override
    public String toString() {
        return "UserImportVO{" +
                "username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}