package org.example.controller;

import org.example.entity.Admin;
import org.example.entity.User;
import org.example.mapper.RepairOrderMapper;
import org.example.mapper.StudentMapper;
import org.example.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

        // 2. 解析管辖楼栋 (例如 "西9,东3" -> ["西9", "东3"])
        List<String> buildingNos = Collections.emptyList();
        if (admin.getManageBuilding() != null && !admin.getManageBuilding().isEmpty()) {
            // 中文逗号和英文逗号兼容处理
            String buildings = admin.getManageBuilding().replace("，", ",");
            buildingNos = Arrays.asList(buildings.split(","));
        }

        // 3. 获取统计数据
        int pendingRepairsCount = 0;
        List<Map<String, Object>> latestRepairs = Collections.emptyList();

        if (!buildingNos.isEmpty()) {
            pendingRepairsCount = repairOrderMapper.countPendingByBuildingNos(buildingNos);
            latestRepairs = repairOrderMapper.findLatestPendingByBuildingNos(buildingNos);
        }

        // 获取待分配学生数 (这里简化为全局未分配，如果需要只统计该宿管负责的学院/专业，需调整SQL)
        int unassignedStudentCount = studentMapper.countUnassignedStudents();

        // 4. 存入 Model
        model.addAttribute("admin", admin);
        model.addAttribute("user", user);
        model.addAttribute("pendingRepairsCount", pendingRepairsCount);
        model.addAttribute("unassignedStudentCount", unassignedStudentCount);
        model.addAttribute("latestRepairs", latestRepairs);
        model.addAttribute("buildingListStr", String.join(" | ", buildingNos));

        return "sys_admin/dorm_admin_index"; // 注意：这里我复用了 sys_admin 目录，或者您可以新建 dorm_admin 目录
    }



    @GetMapping("/admin/repair")
    public String repairList(@org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "pending") String status,
                             HttpSession session, Model model) {
        // 权限校验
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) {
            return "redirect:/login";
        }

        // 解析管辖楼栋 (复用之前的逻辑)
        List<String> buildingNos = java.util.Collections.emptyList();
        if (admin.getManageBuilding() != null && !admin.getManageBuilding().isEmpty()) {
            String buildings = admin.getManageBuilding().replace("，", ",");
            buildingNos = Arrays.asList(buildings.split(","));
        }

        // 查询报修单
        List<Map<String, Object>> repairs = java.util.Collections.emptyList();
        if (!buildingNos.isEmpty()) {
            // 调用 Service 查询指定楼栋和状态的报修单
            repairs = repairService.findRepairsByBuildingNos(buildingNos, status);
        }

        model.addAttribute("repairs", repairs);
        model.addAttribute("status", status); // 用于前端回显当前状态
        model.addAttribute("admin", admin);

        // 返回报修管理页面 (请确保 templates 目录下有 repair_manage.html 或 sys_admin/repair_list.html)
        // 这里假设复用 repair_manage.html，你也可以新建一个 sys_admin/repair_list.html
        return "repair_manage";
    }

    /**
     * 2. 接单处理 (对应 /admin/repair/process/{id})
     * 宿管点击“接单”后，状态变为 processing
     */
    @GetMapping("/admin/repair/process/{id}")
    public String processRepair(@org.springframework.web.bind.annotation.PathVariable Integer id,
                                HttpSession session) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) {
            return "redirect:/login";
        }

        // 更新状态为 "processing" (处理中)，并记录处理人 (adminId)
        // updateRepairStatus 方法在 RepairService 中已存在
        repairService.updateRepairStatus(id, "processing", admin.getAdminId(), null);

        // 处理完后重定向回首页，或者重定向回报修列表页
        return "redirect:/dorm_admin_index";
    }
}