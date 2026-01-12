package org.example.mapper;

import org.example.entity.Building;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BuildingMapper {

    // 查询所有楼栋
    @Select("SELECT * FROM Building")
    List<Building> findAll();

    // 根据ID查询
    @Select("SELECT * FROM Building WHERE building_id = #{id}")
    Building findById(Integer id);

    // 新增楼栋
    @Insert("INSERT INTO Building(building_no, building_name, room_count, gender_type, built_year, status, address, description) " +
            "VALUES(#{buildingNo}, #{buildingName}, #{roomCount}, #{genderType}, #{builtYear}, 'active', #{address}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "buildingId")
    int insert(Building building);

    // 更新楼栋信息
    @Update("UPDATE Building SET building_no=#{buildingNo}, building_name=#{buildingName}, " +
            "gender_type=#{genderType}, status=#{status}, description=#{description} " +
            "WHERE building_id = #{buildingId}")
    int update(Building building);

    // 删除楼栋 (逻辑删除或物理删除，这里演示物理删除)
    @Delete("DELETE FROM Building WHERE building_id = #{id}")
    int deleteById(Integer id);

    @Select("SELECT COUNT(*) FROM Building")
    int countAll();


    @Select("SELECT * FROM Building WHERE building_no = #{buildingNo}")
    Building findByBuildingNo(String buildingNo);
}