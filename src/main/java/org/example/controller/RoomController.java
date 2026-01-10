package org.example.controller;

import org.example.entity.Building;
import org.example.entity.Room;
import org.example.service.BuildingService;
import org.example.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private BuildingService buildingService; // 需要注入楼栋服务，用于下拉框

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
    public String save(Room room) {
        roomService.saveRoom(room);
        return "redirect:/admin/room/list";
    }

    // 4. 删除
    @GetMapping("/delete")
    public String delete(@RequestParam Integer id) {
        roomService.deleteRoom(id);
        return "redirect:/admin/room/list";
    }
}