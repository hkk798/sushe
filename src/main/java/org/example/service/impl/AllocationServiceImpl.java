package org.example.service.impl;

import org.example.entity.AllocationRecord;
import org.example.entity.Room;
import org.example.entity.Student;
import org.example.mapper.AllocationRecordMapper;
import org.example.mapper.StudentMapper;
import org.example.mapper.RoomMapper;
import org.example.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AllocationServiceImpl implements AllocationService {

    @Autowired
    private AllocationRecordMapper allocationMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public List<Student> getUnallocatedStudents() {
        return allocationMapper.findUnallocatedStudents();
    }

    @Override
    public List<Room> getAvailableRooms(Integer studentId) {
        Student student = studentMapper.findById(studentId);
        if (student == null)
            return List.of();

        return allocationMapper.findAvailableRoomsByGender(student.getGender());
    }

    @Override
    @Transactional
    public void assignDorm(Integer studentId, Integer roomId, Integer adminId) {
        AllocationRecord record = new AllocationRecord();
        record.setStudentId(studentId);
        record.setRoomId(roomId);
        record.setAdminId(adminId);
        record.setAllocateDate(LocalDate.now());
        record.setStatus("active");
        record.setFeeStatus("unpaid");

        // 获取并分配床位号
        String bedNo = getNextAvailableBedNo(roomId);
        if (bedNo == null) {
            throw new RuntimeException("房间已无可用床位");
        }
        record.setBedNo(bedNo);

        allocationMapper.insert(record);

        // Java层更新房间人数
        updateRoomCountInJava(roomId, 1);

        System.out.println("分配宿舍: 学生" + studentId + " 房间" + roomId + " 床位" + bedNo);
    }

    @Override
    @Transactional
    public void changeDorm(Integer studentId, Integer newRoomId, Integer adminId) {
        try {
            // 1. 获取学生当前分配信息
            AllocationRecord currentAllocation = findCurrentAllocation(studentId);
            if (currentAllocation == null) {
                throw new RuntimeException("学生当前没有有效的宿舍分配");
            }

            Integer oldRoomId = currentAllocation.getRoomId();

            // 2. 检查新旧房间是否相同
            if (oldRoomId.equals(newRoomId)) {
                throw new RuntimeException("新房间与当前房间相同，无需换寝");
            }

            // 3. 检查新房间是否有空位
            Room newRoom = roomMapper.findById(newRoomId);
            if (newRoom == null) {
                throw new RuntimeException("新房间不存在");
            }

            int currentActiveCount = countActiveStudentsInRoom(newRoomId);
            if (currentActiveCount >= newRoom.getCapacity()) {
                throw new RuntimeException("新房间已满，无法入住");
            }

            // 4. 结束当前有效的入住记录
            allocationMapper.completeActiveRecord(studentId);

            // 5. 减少旧房间的入住人数
            updateRoomCountInJava(oldRoomId, -1);

            // 6. 分配新宿舍 (assignDorm 内部会自动增加新房间人数)
            assignDorm(studentId, newRoomId, adminId);

            System.out.println("换寝室成功: 学生" + studentId + " 从房间" + oldRoomId + " 换到房间" + newRoomId);

        } catch (Exception e) {
            e.printStackTrace();
            // 这里抛出异常，Controller 会捕获并弹窗
            throw new RuntimeException(e.getMessage());
        }
    }

    // ✅ [新增] 必须包含这个方法，供 UserService 禁用账号时调用
    @Override
    @Transactional
    public void removeStudentFromDorm(Integer studentId) {
        try {
            // 1. 查找学生当前生效的宿舍分配
            AllocationRecord current = studentMapper.findCurrentDormAllocation(studentId);

            if (current != null && "active".equals(current.getStatus())) {
                Integer roomId = current.getRoomId();

                // 2. 结束该条分配记录
                allocationMapper.completeActiveRecord(studentId);

                // 3. 减少房间的当前入住人数
                updateRoomCountInJava(roomId, -1);

                System.out.println("账号禁用联动: 已移除学生 " + studentId + " 的宿舍分配 (房间 " + roomId + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("联动退宿失败: " + e.getMessage());
        }
    }

    // ================= 以下为私有辅助方法 =================

    private AllocationRecord findCurrentAllocation(Integer studentId) {
        return studentMapper.findCurrentDormAllocation(studentId);
    }

    private String getNextAvailableBedNo(Integer roomId) {
        try {
            Room room = roomMapper.findById(roomId);
            if (room == null) return null;

            Integer capacity = room.getCapacity();
            List<AllocationRecord> allocations = allocationMapper.findByRoomId(roomId);

            Set<String> occupiedBeds = new HashSet<>();
            for (AllocationRecord ar : allocations) {
                if (ar.getBedNo() != null && "active".equals(ar.getStatus())) {
                    occupiedBeds.add(ar.getBedNo());
                }
            }

            List<String> standardBeds = generateStandardBedNos(capacity);
            for (String bedNo : standardBeds) {
                if (!occupiedBeds.contains(bedNo)) {
                    return bedNo;
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> generateStandardBedNos(Integer capacity) {
        List<String> bedNos = new ArrayList<>();
        int rows = (capacity + 1) / 2;
        for (int i = 1; i <= rows; i++) {
            String row = String.format("%02d", i);
            bedNos.add("A" + row);
            if (bedNos.size() < capacity) {
                bedNos.add("B" + row);
            }
        }
        return bedNos;
    }

    private void updateRoomCountInJava(Integer roomId, int delta) {
        try {
            Room room = roomMapper.findById(roomId);
            if (room != null) {
                int newCount = room.getCurrentCount() + delta;
                if (newCount < 0) newCount = 0;
                if (newCount > room.getCapacity()) newCount = room.getCapacity();

                room.setCurrentCount(newCount);
                roomMapper.update(room);
            }
        } catch (Exception e) {
            System.err.println("Java更新房间人数失败: " + e.getMessage());
        }
    }

    private int countActiveStudentsInRoom(Integer roomId) {
        try {
            List<AllocationRecord> allocations = allocationMapper.findByRoomId(roomId);
            int count = 0;
            for (AllocationRecord ar : allocations) {
                if ("active".equals(ar.getStatus())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Transactional
    public void fixRoomCountConsistency() {
        // ... (省略具体的打印日志，保持原有逻辑即可)
        System.out.println("开始修复房间人数一致性...");
        try {
            List<Room> allRooms = roomMapper.findAll();
            for (Room room : allRooms) {
                int actualActiveCount = countActiveStudentsInRoom(room.getRoomId());
                if (room.getCurrentCount() != actualActiveCount) {
                    room.setCurrentCount(actualActiveCount);
                    roomMapper.update(room);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}