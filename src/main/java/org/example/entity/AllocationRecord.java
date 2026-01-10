package org.example.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AllocationRecord {
    private Integer recordId;       // 对应 record_id
    private Integer studentId;      // 对应 student_id
    private Integer roomId;         // 对应 room_id
    private Integer adminId;        // 对应 admin_id (操作人)
    private LocalDate allocateDate; // 对应 allocate_date
    private LocalDate checkInDate;  // 对应 check_in_date
    private LocalDate checkOutDate; // 对应 check_out_date
    private String status;          // active, completed
    private String feeStatus;       // paid, unpaid, overdue
    private String bedNo;           // 对应 bed_no
    private String contractNo;      // 对应 contract_no
    private String remarks;         // 对应 remarks
    private Room room;

    // 扩展字段 (用于前端显示，数据库里没有)
    private String studentName;
    private String studentNo;
    private String roomName;    // 如 "西9-101"
    private String buildingName;


    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public LocalDate getAllocateDate() {
        return allocateDate;
    }

    public void setAllocateDate(LocalDate allocateDate) {
        this.allocateDate = allocateDate;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFeeStatus() {
        return feeStatus;
    }

    public void setFeeStatus(String feeStatus) {
        this.feeStatus = feeStatus;
    }

    public String getBedNo() {
        return bedNo;
    }

    public void setBedNo(String bedNo) {
        this.bedNo = bedNo;
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // --- 扩展字段的 Getter/Setter ---

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public boolean isActive() {

        return "active".equals(status);
    }

}