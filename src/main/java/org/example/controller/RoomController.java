package org.example.controller;

import org.example.entity.Building;
import org.example.entity.Room;
import org.example.service.BuildingService;
import org.example.service.RoomService;
import org.example.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BuildingService buildingService; // 需要注入楼栋服务，用于下拉框

    @Autowired
    private SystemLogService systemLogService;

    // 1. 房间列表
    @GetMapping("/list")
    public String list(@RequestParam(required = false) Integer buildingId, Model model) {
        List<Room> rooms;

        if (buildingId != null) {
            // 如果传了楼栋ID，就只查这栋楼的
            rooms = roomService.getRoomsByBuildingId(buildingId);
            // 把 buildingId 存回去，方便页面上的“新增房间”按钮自动选中该楼栋（可选优化）
            model.addAttribute("currentBuildingId", buildingId);
        } else {
            // 没传参数，查所有
            rooms = roomService.getAllRooms();
        }

        model.addAttribute("rooms", rooms);
        return "sys_admin/room_list";
    }

    @GetMapping("/form")
    public String form(@RequestParam(required = false) Integer id,
                       @RequestParam(required = false) Integer buildingId, // 👈 新增参数
                       Model model) {
        Room room;
        if (id != null) {
            room = roomService.getRoomById(id);
        } else {
            room = new Room();
            room.setCapacity(4);
            room.setYearlyFee(new java.math.BigDecimal("1200.00"));
            // 👇 如果有预设的楼栋ID，直接填进去
            if (buildingId != null) {
                room.setBuildingId(buildingId);
            }
        }

        List<Building> buildings = buildingService.getAllBuildings();
        model.addAttribute("room", room);
        model.addAttribute("buildings", buildings);
        return "sys_admin/room_form";
    }

    // 3. 保存
    @PostMapping("/save")
    public String save(Room room, HttpSession session, HttpServletRequest request, Model model) { // 👈 添加 Model 参数

        // --- [新增] 重复性检查逻辑开始 ---
        Room existingRoom = roomService.getRoomByBuildingIdAndRoomNo(room.getBuildingId(), room.getRoomNo());

        if (existingRoom != null) {
            // 如果是新增 (roomId为空) 且 查到了同名房间 -> 冲突
            // 如果是编辑 (roomId不为空) 且 查到了同名房间，但ID不同 -> 冲突
            if (room.getRoomId() == null || !existingRoom.getRoomId().equals(room.getRoomId())) {
                model.addAttribute("errorMessage", "操作失败：该楼栋下已存在房间号 " + room.getRoomNo());
                return "error/room_error"; // 👈 跳转到错误反馈页面
            }
        }
        // --- [新增] 重复性检查逻辑结束 ---

        if (room.getRoomId() != null) {
            // 从数据库取出该房间的旧数据（主要是为了拿真实的 currentCount）
            Room oldRoom = roomService.getRoomById(room.getRoomId());

            if (oldRoom != null) {
                // 如果 管理员填写的容量 < 实际入住人数
                if (room.getCapacity() < oldRoom.getCurrentCount()) {
                    model.addAttribute("errorMessage",
                            "操作失败：容量不能小于当前入住人数！(当前已住: " + oldRoom.getCurrentCount() + "人)");
                    return "error/room_error"; // 跳转到错误页面
                }
            }
        }



        String actionType = (room.getRoomId() == null) ? "新增房间" : "编辑房间";

        roomService.saveRoom(room);

        // 日志记录
        User admin = (User) session.getAttribute("currentUser");
        String operator = (admin != null) ? admin.getUsername() : "Unknown";

        systemLogService.recordLog(
                operator,
                actionType,
                "保存房间: " + room.getRoomNo(),
                request.getRemoteAddr()
        );

        return "redirect:/admin/room/list";
    }

    // 4. 删除
    @GetMapping("/delete")
    public String delete(@RequestParam Integer id, HttpSession session, HttpServletRequest request) {
        roomService.deleteRoom(id);

        // [新增] 日志
        User admin = (User) session.getAttribute("currentUser");
        String operator = (admin != null) ? admin.getUsername() : "Unknown";

        systemLogService.recordLog(
                operator,
                "删除房间",
                "删除了房间 ID: " + id,
                request.getRemoteAddr()
        );

        return "redirect:/admin/room/list";
    }
}