package org.example.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Building {
    private Integer buildingId;     // 主键
    private String buildingNo;      // 楼栋编号 (如: 西9)
    private String buildingName;    // 楼栋名称
    private Integer roomCount;      // 房间数
    private String genderType;      // male, female, mixed
    private Integer builtYear;      // 建成年份
    private String status;          // active, inactive, repair
    private String address;         // 地址
    private String description;     // 描述
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}