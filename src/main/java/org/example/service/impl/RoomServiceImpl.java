package org.example.service.impl;

import org.example.entity.Room;
import org.example.mapper.RoomMapper;
import org.example.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public List<Room> getAllRooms() {
        return roomMapper.findAll();
    }

    @Override
    public Room getRoomById(Integer id) {
        return roomMapper.findById(id);
    }

    @Override
    public void saveRoom(Room room) {
        if (room.getRoomId() == null) {
            // 新增逻辑
            if (room.getCurrentCount() == null) room.setCurrentCount(0);
            if (room.getStatus() == null) room.setStatus("available");
            roomMapper.insert(room);
        } else {
            // 更新逻辑
            roomMapper.update(room);
        }
    }

    @Override
    public void deleteRoom(Integer id) {
        roomMapper.deleteById(id);
    }

    @Override
    public List<Room> getRoomsByBuildingId(Integer buildingId) {
        return roomMapper.findByBuildingId(buildingId);
    }


    @Override
    public List<Room> getRoomsByBuildingNames(List<String> buildingNames) {
        if (buildingNames == null || buildingNames.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return roomMapper.findByBuildingNames(buildingNames);
    }


    @Override
    public Room getRoomByBuildingIdAndRoomNo(Integer buildingId, String roomNo) {
        return roomMapper.findByBuildingIdAndRoomNo(buildingId, roomNo);
    }
}