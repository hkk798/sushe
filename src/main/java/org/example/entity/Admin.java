package org.example.entity;

/**
 * 管理员信息表实体类
 */
public class Admin {
    private Integer adminId;
    private Integer userId;
    private String adminNo;
    private String adminType;       // building, system
    private String position;        // 职位（长度100）
    private String workPhone;
    private String manageBuilding;
    
    // 关联的User对象
    private User user;
    
    // 无参构造
    public Admin() {
    }
    
    // Getter和Setter
    public Integer getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getAdminNo() {
        return adminNo;
    }
    
    public void setAdminNo(String adminNo) {
        this.adminNo = adminNo;
    }
    
    public String getAdminType() {
        return adminType;
    }
    
    public void setAdminType(String adminType) {
        this.adminType = adminType;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getWorkPhone() {
        return workPhone;
    }
    
    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }
    
    public String getManageBuilding() {
        return manageBuilding;
    }
    
    public void setManageBuilding(String manageBuilding) {
        this.manageBuilding = manageBuilding;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // 业务方法
    public boolean isBuildingAdmin() {
        return "building".equals(adminType);
    }
    
    public boolean isSystemAdmin() {
        return "system".equals(adminType);
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", adminNo='" + adminNo + '\'' +
                ", adminType='" + adminType + '\'' +
                ", position='" + position + '\'' +
                '}';
    }
}