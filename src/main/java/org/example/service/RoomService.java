package org.example.service;

import org.example.entity.Room;
import java.util.List;

public interface RoomService {
    List<Room> getAllRooms();
    Room getRoomById(Integer id);
    void saveRoom(Room room);
    void deleteRoom(Integer id);
    // 添加这一行
    List<Room> getRoomsByBuildingId(Integer buildingId);
}