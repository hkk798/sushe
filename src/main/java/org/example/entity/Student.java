package org.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Student {
    private Integer studentId;      // 对应 student_id
    private Integer userId;         // 对应 user_id
    private String studentNo;       // 学号
    private String major;           // 专业
    private String className;       // 班级
    private String gender;          // 性别 (M/F)
    private User user;

    // 扩展字段 (用于显示姓名，数据来自 User 表)
    private String realName;


    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public User getUser( ) {
        return user;
   }

    public void setUser(User user) {
        this.user  = user;
    }

}