package org.example.service.impl;

import org.example.entity.AllocationRecord;
import org.example.entity.Room;
import org.example.entity.Student;
import org.example.mapper.AllocationRecordMapper;
import org.example.mapper.StudentMapper;
import org.example.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AllocationServiceImpl implements AllocationService {

    @Autowired
    private AllocationRecordMapper allocationMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Override
    public List<Student> getUnallocatedStudents() {
        return allocationMapper.findUnallocatedStudents();
    }

    @Override
    public List<Room> getAvailableRooms(Integer studentId) {
        // 先查学生性别
        // (注意：请确保 StudentMapper 里有 findById 方法，如果没有请补上)
        Student student = studentMapper.findById(studentId);
        if (student == null) return List.of();

        return allocationMapper.findAvailableRoomsByGender(student.getGender());
    }

    @Override
    @Transactional
    public void assignDorm(Integer studentId, Integer roomId, Integer adminId) {
        AllocationRecord record = new AllocationRecord();
        record.setStudentId(studentId);
        record.setRoomId(roomId);
        record.setAdminId(adminId); // 必须记录是谁分配的
        record.setAllocateDate(LocalDate.now());
        record.setStatus("active");
        record.setFeeStatus("unpaid");

        // 只管插入，Room表的人数更新交给数据库触发器处理
        allocationMapper.insert(record);
    }
}