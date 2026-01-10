package org.example.mapper;

import org.example.entity.Room;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface RoomMapper {

    // 联表查询：同时查出房间信息和所属楼栋名称
    @Select("SELECT r.*, b.building_name " +
            "FROM Room r " +
            "LEFT JOIN Building b ON r.building_id = b.building_id " +
            "ORDER BY b.building_name, r.room_no")
    List<Room> findAll();

    @Select("SELECT * FROM Room WHERE room_id = #{id}")
    Room findById(Integer id);

    // 新增房间
    @Insert("INSERT INTO Room(building_id, room_no, capacity, current_count, gender_type, status, yearly_fee, floor) " +
            "VALUES(#{buildingId}, #{roomNo}, #{capacity}, 0, #{genderType}, 'available', #{yearlyFee}, #{floor})")
    @Options(useGeneratedKeys = true, keyProperty = "roomId")
    int insert(Room room);

    // 更新房间
    @Update("UPDATE Room SET building_id=#{buildingId}, room_no=#{roomNo}, capacity=#{capacity}, " +
            "gender_type=#{genderType}, yearly_fee=#{yearlyFee}, floor=#{floor}, status=#{status} " +
            "WHERE room_id = #{roomId}")
    int update(Room room);

    @Delete("DELETE FROM Room WHERE room_id = #{id}")
    int deleteById(Integer id);

    // 根据楼栋ID删除房间 (删除楼栋前可能需要先删除房间)
    @Delete("DELETE FROM Room WHERE building_id = #{buildingId}")
    int deleteByBuildingId(Integer buildingId);

    // 添加根据 buildingId 查询的方法
    @Select("SELECT r.*, b.building_name " +
            "FROM Room r " +
            "LEFT JOIN Building b ON r.building_id = b.building_id " +
            "WHERE r.building_id = #{buildingId} " + // 👈 加了 WHERE 条件
            "ORDER BY r.room_no")
    List<Room> findByBuildingId(Integer buildingId);
}