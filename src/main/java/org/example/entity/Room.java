package org.example.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Room {
    private Integer roomId;         // 主键
    private Integer buildingId;     // 外键：所属楼栋ID
    private String roomNo;          // 房间号 (如: 101)
    private Integer capacity;       // 床位数 (默认4)
    private Integer currentCount;   // 当前入住人数
    private String genderType;      // 性别限制 (male/female)
    private String status;          // available(空闲), full(满员)
    private BigDecimal yearlyFee;   // 住宿费
    private Integer floor;          // 楼层

    // 扩展字段 (数据库表中没有，仅用于前端展示)
    private String buildingName;    // 所属楼栋名称



    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Integer buildingId) {
        this.buildingId = buildingId;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public String getGenderType() {
        return genderType;
    }

    public void setGenderType(String genderType) {
        this.genderType = genderType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getYearlyFee() {
        return yearlyFee;
    }

    public void setYearlyFee(BigDecimal yearlyFee) {
        this.yearlyFee = yearlyFee;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }
}