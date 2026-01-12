package org.example.controller;

import org.example.entity.Admin;
import org.example.entity.Room;
import org.example.entity.User;
import org.example.mapper.RepairOrderMapper;
import org.example.mapper.StudentMapper;
import org.example.service.RoomService;
import org.example.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class DormAdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private RepairOrderMapper repairOrderMapper;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private org.example.service.RepairService repairService;
    @Autowired
    private RoomService roomService; // 确保注入了 RoomService



    /**
     * 宿舍管理员首页
     */
    @GetMapping("/dorm_admin_index")
    public String index(HttpSession session, Model model) {
        // 1. 权限校验
        User user = (User) session.getAttribute("currentUser");
        Admin admin = (Admin) session.getAttribute("currentAdmin");

        if (user == null || !"building_admin".equals(user.getRole()) || admin == null) {
            return "redirect:/login";
        }

        // 2. 解析管辖楼栋
        List<String> buildingNos = Collections.emptyList();
        if (admin.getManageBuilding() != null && !admin.getManageBuilding().isEmpty()) {
            String buildings = admin.getManageBuilding().replace("，", ",");
            buildingNos = Arrays.asList(buildings.split(","));
        }

        // 3. 获取统计数据
        int pendingRepairsCount = 0;
        List<Map<String, Object>> latestRepairs = Collections.emptyList();

        if (!buildingNos.isEmpty()) {
            // 修改：使用 countUnfinishedByBuildingNos 统计所有未完成（pending + processing）
            pendingRepairsCount = repairOrderMapper.countUnfinishedByBuildingNos(buildingNos);
            // 修改：使用 findLatestUnfinishedByBuildingNos 获取所有未完成列表
            latestRepairs = repairOrderMapper.findLatestUnfinishedByBuildingNos(buildingNos);
        }

        // 获取待分配学生数
        int unassignedStudentCount = studentMapper.countUnassignedStudents();

        // 4. 存入 Model
        model.addAttribute("admin", admin);
        model.addAttribute("user", user);
        // 这里虽然变量名叫 pendingRepairsCount，但实际值已经是 "未完成总数"
        model.addAttribute("pendingRepairsCount", pendingRepairsCount);
        model.addAttribute("unassignedStudentCount", unassignedStudentCount);
        model.addAttribute("latestRepairs", latestRepairs);
        model.addAttribute("buildingListStr", String.join(" | ", buildingNos));

        return "sys_admin/dorm_admin_index";
    }

    /**
     * 报修管理列表页
     */
    @GetMapping("/admin/repair")
    public String repairList(@RequestParam(required = false, defaultValue = "pending") String status,
                             HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) {
            return "redirect:/login";
        }

        List<String> buildingNos = Collections.emptyList();
        if (admin.getManageBuilding() != null && !admin.getManageBuilding().isEmpty()) {
            String buildings = admin.getManageBuilding().replace("，", ",");
            buildingNos = Arrays.asList(buildings.split(","));
        }

        List<Map<String, Object>> repairs = Collections.emptyList();
        if (!buildingNos.isEmpty()) {
            repairs = repairService.findRepairsByBuildingNos(buildingNos, status);
        }

        model.addAttribute("repairs", repairs);
        model.addAttribute("status", status);
        model.addAttribute("admin", admin);

        return "repair_manage";
    }

    /**
     * 接单处理 (pending -> processing)
     */
    @GetMapping("/admin/repair/process/{id}")
    public String processRepair(@PathVariable Integer id,
                                HttpSession session) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) {
            return "redirect:/login";
        }
        repairService.updateRepairStatus(id, "processing", admin.getAdminId(), null);
        return "redirect:/dorm_admin_index";
    }

    /**
     * 完成报修处理 (processing/pending -> completed)
     */
    @PostMapping("/admin/repair/complete")
    public String completeRepair(@RequestParam Integer orderId,
                                 @RequestParam String remarks,
                                 @RequestParam(required = false, defaultValue = "index") String source,
                                 HttpSession session) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) {
            return "redirect:/login";
        }

        repairService.updateRepairStatus(orderId, "completed", admin.getAdminId(), remarks);

        // 如果来源是列表页，则返回列表页
        if ("list".equals(source)) {
            return "redirect:/admin/repair?status=pending";
        }
        // 默认返回工作台
        return "redirect:/dorm_admin_index";
    }

    @GetMapping("/admin/dorm/rooms")
    public String myManagedRooms(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) return "redirect:/login";

        // 1. 解析管辖楼栋
        List<String> buildingNos = Collections.emptyList();
        if (admin.getManageBuilding() != null && !admin.getManageBuilding().isEmpty()) {
            // 替换中文逗号 -> 分割 -> 去除首尾空格
            String[] split = admin.getManageBuilding().replace("，", ",").split(",");
            buildingNos = Arrays.stream(split)
                    .map(String::trim) // <--- 新增：去除空格，防止 " 西9" 查不到
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        // 2. 查询房间
        List<Room> rooms = Collections.emptyList();
        if (!buildingNos.isEmpty()) {
            rooms = roomService.getRoomsByBuildingNames(buildingNos);
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("admin", admin);
        return "sys_admin/dorm_room_list";
    }

    /**
     * [新增] AJAX获取房间内的学生信息
     */
    @GetMapping("/admin/dorm/room/{id}/students")
    @ResponseBody
    public List<Map<String, Object>> getRoomStudents(@PathVariable Integer id, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) return Collections.emptyList();

        // 查询该房间的学生
        return studentMapper.findStudentsByRoomId(id);
    }


}