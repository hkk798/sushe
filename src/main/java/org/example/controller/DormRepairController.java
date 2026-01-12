package org.example.controller;

import org.example.entity.Admin;
import org.example.entity.RepairOrder;
import org.example.service.DormService; // 引入DormService
import org.example.service.RepairService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dorm/repair")
public class DormRepairController {

    @Autowired
    private RepairService repairService;

    @Autowired
    private DormService dormService; // 注入DormService

    /**
     * 1. 报修管理列表页
     */
    @GetMapping("")
    public String list(HttpSession session,
                       @RequestParam(value = "status", defaultValue = "pending") String status,
                       Model model) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) return "redirect:/login";

        List<String> buildingNos = getBuildingNos(admin);
        if (buildingNos.isEmpty()) {
            model.addAttribute("error", "您当前未管辖任何楼栋");
            // 这里建议也返回一个空的列表防止模板报错，或者在模板里做好判空
            model.addAttribute("repairs", Collections.emptyList());
            model.addAttribute("currentStatus", status);
            return "sys_admin/repair_manage";
        }

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
        if (admin == null) return "redirect:/login"; // 增加安全校验

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
        if (admin == null) return "redirect:/login";

        repairService.updateRepairStatus(orderId, "completed", admin.getAdminId(), remarks);
        return "redirect:/dorm/repair?status=processing";
    }

    /**
     * [新增] 宿管查看报修详情
     */
    @GetMapping("/detail")
    public String detail(@RequestParam("orderId") Integer orderId,
                         HttpSession session,
                         Model model) {
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) return "redirect:/login";

        try {
            // 获取报修单详情
            RepairOrder repairOrder = repairService.getRepairOrderDetail(orderId);
            if (repairOrder == null) {
                model.addAttribute("errorMessage", "报修单不存在");
                return "repair_detail";
            }

            // 获取宿舍位置信息 (宿管端也需要展示位置)
            String repairLocation = "未知位置";
            try {
                Map<String, Object> dormInfo = dormService.getStudentDormInfo(repairOrder.getStudentId());
                if (dormInfo != null) {
                    repairLocation = repairService.getRepairLocation(dormInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 处理图片路径
            List<String> imagePaths = new ArrayList<>();
            if (repairOrder.getImages() != null && !repairOrder.getImages().isEmpty()) {
                imagePaths = Arrays.asList(repairOrder.getImages().split(","));
            }

            model.addAttribute("repairOrder", repairOrder);
            model.addAttribute("repairLocation", repairLocation);
            model.addAttribute("imagePaths", imagePaths);

            // 关键：设置返回按钮的链接，覆盖默认的学生主页
            model.addAttribute("backUrl", "/dorm/repair");

            return "repair_detail";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "加载详情失败: " + e.getMessage());
            return "repair_detail";
        }
    }

    private List<String> getBuildingNos(Admin admin) {
        if (admin.getManageBuilding() == null || admin.getManageBuilding().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(admin.getManageBuilding().replace("，", ",").split(","));
    }
}