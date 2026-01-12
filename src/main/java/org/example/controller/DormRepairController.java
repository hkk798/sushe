package org.example.controller;

import org.example.entity.Admin;
import org.example.entity.User;
import org.example.service.RepairService;
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
@RequestMapping("/dorm/repair")
public class DormRepairController {

    @Autowired
    private RepairService repairService;

    /**
     * 1. 报修管理列表页
     * 对应 Dashboard 的 "报修处理" 链接
     */
    @GetMapping("")
    public String list(HttpSession session,
                       @RequestParam(value = "status", defaultValue = "pending") String status,
                       Model model) {
        // 1. 权限与管辖范围校验
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) return "redirect:/login";

        // 2. 解析管辖楼栋
        List<String> buildingNos = getBuildingNos(admin);
        if (buildingNos.isEmpty()) {
            model.addAttribute("error", "您当前未管辖任何楼栋");
            return "sys_admin/repair_manage";
        }

        // 3. 查询数据
        List<Map<String, Object>> repairs = repairService.findRepairsByBuildingNos(buildingNos, status);

        model.addAttribute("repairs", repairs);
        model.addAttribute("currentStatus", status);
        return "sys_admin/repair_manage";
    }

    /**
     * 2. 开始处理 (状态 pending -> processing)
     */
    @GetMapping("/process/{orderId}")
    public String process(@PathVariable Integer orderId, HttpSession session) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        // 调用Service更新状态并记录处理人
        repairService.updateRepairStatus(orderId, "processing", admin.getAdminId(), null);
        return "redirect:/dorm/repair?status=pending";
    }

    /**
     * 3. 完成处理 (状态 processing -> completed)
     */
    @PostMapping("/complete")
    public String complete(@RequestParam Integer orderId,
                           @RequestParam String remarks,
                           HttpSession session) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        repairService.updateRepairStatus(orderId, "completed", admin.getAdminId(), remarks);
        return "redirect:/dorm/repair?status=processing";
    }

    // 辅助方法：解析管理员的 manage_building 字段
    private List<String> getBuildingNos(Admin admin) {
        if (admin.getManageBuilding() == null || admin.getManageBuilding().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(admin.getManageBuilding().replace("，", ",").split(","));
    }
}