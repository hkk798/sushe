package org.example.entity;

import java.time.LocalDateTime;

public class SystemLog {
    private Integer id;
    private String operator;   // 操作人
    private String action;     // 动作 (如: 登录, 添加用户)
    private String detail;     // 详情
    private String ip;         // IP地址
    private LocalDateTime createTime; // 时间

    // 无参构造
    public SystemLog() {}

    // 方便记录日志的构造函数
    public SystemLog(String operator, String action, String detail, String ip) {
        this.operator = operator;
        this.action = action;
        this.detail = detail;
        this.ip = ip;
        this.createTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}