package org.example.entity;
import java.util.Date;
/**
 * 报修单表实体类
 * 对应数据库表：RepairOrder
 */
public class RepairOrder {
    private Integer orderId;          // 报修单ID
    private Integer studentId;        // 报修学生ID
    private Integer roomId;           // 报修房间ID
    private String title;             // 报修标题
    private String description;       // 问题描述
    private String category;          // 报修类别
    private Date submitTime;          // 提交时间
    private String status;            // 处理状态
    private Integer processorId;      // 处理人ID
    private Date processTime;         // 处理时间
    private String processResult;     // 处理结果
    
    // 关联的对象
    private Student student;
    private Room room;
    private Admin processor;
    
    // 无参构造
    public RepairOrder() {
    }
    
    // 全参构造
    public RepairOrder(Integer orderId, Integer studentId, Integer roomId, 
                       String title, String description, String category, 
                       Date submitTime, String status, Integer processorId, 
                       Date processTime, String processResult) {
        this.orderId = orderId;
        this.studentId = studentId;
        this.roomId = roomId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.submitTime = submitTime;
        this.status = status;
        this.processorId = processorId;
        this.processTime = processTime;
        this.processResult = processResult;
    }
    
    // Getter和Setter方法
    public Integer getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Date getSubmitTime() {
        return submitTime;
    }
    
    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getProcessorId() {
        return processorId;
    }
    
    public void setProcessorId(Integer processorId) {
        this.processorId = processorId;
    }
    
    public Date getProcessTime() {
        return processTime;
    }
    
    public void setProcessTime(Date processTime) {
        this.processTime = processTime;
    }
    
    public String getProcessResult() {
        return processResult;
    }
    
    public void setProcessResult(String processResult) {
        this.processResult = processResult;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public Room getRoom() {
        return room;
    }
    
    public void setRoom(Room room) {
        this.room = room;
    }
    
    public Admin getProcessor() {
        return processor;
    }
    
    public void setProcessor(Admin processor) {
        this.processor = processor;
    }
    
    // 业务方法
    public boolean isPending() {
        return "pending".equals(status);
    }
    
    public boolean isProcessing() {
        return "processing".equals(status);
    }
    
    public boolean isCompleted() {
        return "completed".equals(status);
    }
    
    @Override
    public String toString() {
        return "RepairOrder{" +
                "orderId=" + orderId +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", submitTime=" + submitTime +
                '}';
    }
}