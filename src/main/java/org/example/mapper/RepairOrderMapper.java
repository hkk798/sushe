package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RepairOrderMapper {
    // 统计所有报修单数量
    @Select("SELECT COUNT(*) FROM RepairOrder")
    int countAll();

    // 这里后续还需要添加查询列表等方法
}