package org.example.service;

import org.example.entity.Room;
import org.example.entity.Student;
import java.util.List;

public interface AllocationService {
    List<Student> getUnallocatedStudents();
    List<Room> getAvailableRooms(Integer studentId);
    void assignDorm(Integer studentId, Integer roomId, Integer adminId);

    void changeDorm(Integer studentId, Integer newRoomId, Integer adminId);
}