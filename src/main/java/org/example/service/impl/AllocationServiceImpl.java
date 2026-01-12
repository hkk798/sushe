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
    private RoomMapper roomMapper; // 需要注入RoomMapper

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
        record.setAdminId(adminId); // 必须记录是谁分配的
        record.setAllocateDate(LocalDate.now());
        record.setStatus("active");
        record.setFeeStatus("unpaid");

        // 获取并分配床位号
        String bedNo = getNextAvailableBedNo(roomId);
        if (bedNo == null) {
            throw new RuntimeException("房间已无可用床位");
        }
        record.setBedNo(bedNo);

        // 只管插入，Room表的人数更新交给数据库触发器处理
        allocationMapper.insert(record);

        // 同时，为了确保数据一致性，在Java代码中也更新一次房间人数
        // 这样即使触发器有问题，也能保证数据正确
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

            // 获取当前新房间的active人数
            int currentActiveCount = countActiveStudentsInRoom(newRoomId);
            if (currentActiveCount >= newRoom.getCapacity()) {
                throw new RuntimeException("新房间已满，无法入住");
            }

            // 4. 结束当前有效的入住记录
            allocationMapper.completeActiveRecord(studentId);

            // 5. 减少旧房间的入住人数（Java代码管理）
            updateRoomCountInJava(oldRoomId, -1);

            // 6. 分配新宿舍
            assignDorm(studentId, newRoomId, adminId);

            // assignDorm已经增加了新房间人数，所以这里不需要再增加

            System.out.println("换寝室成功: 学生" + studentId +
                    " 从房间" + oldRoomId + " 换到房间" + newRoomId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("换寝室失败: " + e.getMessage());
        }
    }

    /**
     * 查找学生当前的分配记录
     */
    private AllocationRecord findCurrentAllocation(Integer studentId) {
        // 使用studentMapper中已有的方法
        return studentMapper.findCurrentDormAllocation(studentId);
    }

    /**
     * 获取下一个可用床位号
     */
    private String getNextAvailableBedNo(Integer roomId) {
        try {
            Room room = roomMapper.findById(roomId);
            if (room == null) {
                return null;
            }

            Integer capacity = room.getCapacity();
            // 获取该房间所有已分配床位
            List<AllocationRecord> allocations = allocationMapper.findByRoomId(roomId);

            // 收集已占用的床位号
            Set<String> occupiedBeds = new HashSet<>();
            for (AllocationRecord ar : allocations) {
                if (ar.getBedNo() != null && "active".equals(ar.getStatus())) {
                    occupiedBeds.add(ar.getBedNo());
                }
            }

            // 根据容量生成标准床位号
            List<String> standardBeds = generateStandardBedNos(capacity);

            // 查找第一个未占用的床位
            for (String bedNo : standardBeds) {
                if (!occupiedBeds.contains(bedNo)) {
                    return bedNo;
                }
            }

            return null; // 没有可用床位
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成标准床位号（如4人间：A01, B01, A02, B02）
     */
    private List<String> generateStandardBedNos(Integer capacity) {
        List<String> bedNos = new ArrayList<>();

        if (capacity == 2) {
            bedNos.add("A01");
            bedNos.add("B01");
        } else if (capacity == 4) {
            bedNos.add("A01");
            bedNos.add("B01");
            bedNos.add("A02");
            bedNos.add("B02");
        } else if (capacity == 6) {
            bedNos.add("A01");
            bedNos.add("B01");
            bedNos.add("A02");
            bedNos.add("B02");
            bedNos.add("A03");
            bedNos.add("B03");
        } else if (capacity == 8) {
            bedNos.add("A01");
            bedNos.add("B01");
            bedNos.add("A02");
            bedNos.add("B02");
            bedNos.add("A03");
            bedNos.add("B03");
            bedNos.add("A04");
            bedNos.add("B04");
        } else {
            // 通用规则：每行2个床位，A列和B列
            int rows = (capacity + 1) / 2; // 向上取整
            for (int i = 1; i <= rows; i++) {
                String row = String.format("%02d", i);
                bedNos.add("A" + row);
                if (bedNos.size() < capacity) {
                    bedNos.add("B" + row);
                }
            }
        }

        return bedNos;
    }

    /**
     * 在Java代码中安全地更新房间人数（作为触发器的备份）
     */
    private void updateRoomCountInJava(Integer roomId, int delta) {
        try {
            Room room = roomMapper.findById(roomId);
            if (room != null) {
                int newCount = room.getCurrentCount() + delta;

                // 验证人数范围
                if (newCount < 0) {
                    System.err.println("警告: 房间" + roomId + "人数不能小于0，当前尝试减少" + delta);
                    newCount = 0;
                }
                if (newCount > room.getCapacity()) {
                    System.err.println("警告: 房间" + roomId + "人数超过容量，当前尝试增加" + delta);
                    newCount = room.getCapacity();
                }

                room.setCurrentCount(newCount);
                roomMapper.update(room);

                System.out.println("Java更新房间人数: 房间" + roomId +
                        " 变化" + delta + " 当前" + newCount);
            }
        } catch (Exception e) {
            System.err.println("Java更新房间人数失败: " + e.getMessage());
            // 不抛出异常，因为触发器可能已经处理了
        }
    }

    /**
     * 统计房间中active状态的学生数量
     */
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

    /**
     * 修复房间人数不一致问题（可以在管理后台调用）
     */
    @Transactional
    public void fixRoomCountConsistency() {
        System.out.println("开始修复房间人数一致性...");

        try {
            // 获取所有房间
            List<Room> allRooms = roomMapper.findAll();
            int fixedCount = 0;

            for (Room room : allRooms) {
                // 计算实际的active学生数量
                int actualActiveCount = countActiveStudentsInRoom(room.getRoomId());

                // 如果与数据库记录不一致
                if (room.getCurrentCount() != actualActiveCount) {
                    System.out.println("发现不一致: 房间" + room.getRoomNo() +
                            " 数据库记录=" + room.getCurrentCount() +
                            " 实际active人数=" + actualActiveCount);

                    // 修复
                    room.setCurrentCount(actualActiveCount);
                    roomMapper.update(room);
                    fixedCount++;

                    System.out.println("已修复: 房间" + room.getRoomNo() +
                            " 人数修正为" + actualActiveCount);
                }
            }

            System.out.println("修复完成，共修复" + fixedCount + "个房间");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("修复失败: " + e.getMessage());
        }
    }
}