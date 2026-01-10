package org.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.SystemLog;

import java.util.List;

@Mapper
public interface SystemLogMapper {

    // 查询所有日志，按时间倒序
    @Select("SELECT * FROM system_log ORDER BY create_time DESC")
    List<SystemLog> selectAll();

    // 插入日志
    @Insert("INSERT INTO system_log(operator, action, detail, ip, create_time) " +
            "VALUES(#{operator}, #{action}, #{detail}, #{ip}, #{createTime})")
    void insert(SystemLog log);
}