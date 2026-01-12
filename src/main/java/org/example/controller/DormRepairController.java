package org.example.controller;

import org.example.entity.Admin;
import org.example.entity.RepairOrder;
import org.example.service.DormService;
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
    private DormService dormService; // 注入DormService用于查询位置信息

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
            // 防止模板报错，传入空列表
            model.addAttribute("repairs", Collections.emptyList());
            model.addAttribute("currentStatus", status);
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
        if (admin == null) return "redirect:/login"; // 安全校验

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
        if (admin == null) return "redirect:/login"; // 安全校验

        repairService.updateRepairStatus(orderId, "completed", admin.getAdminId(), remarks);
        return "redirect:/dorm/repair?status=processing";
    }

    /**
     * [新增] 宿管查看报修详情接口
     * 解决点击详情跳转登录页的问题
     */
    @GetMapping("/detail")
    public String detail(@RequestParam("orderId") Integer orderId,
                         HttpSession session,
                         Model model) {
        // 权限校验
        Admin admin = (Admin) session.getAttribute("currentAdmin");
        if (admin == null) return "redirect:/login";

        try {
            // 获取报修单详情
            RepairOrder repairOrder = repairService.getRepairOrderDetail(orderId);
            if (repairOrder == null) {
                model.addAttribute("errorMessage", "报修单不存在");
                return "repair_detail"; // 这里会显示错误信息
            }

            // 获取宿舍位置信息
            String repairLocation = "未知位置";
            try {
                // 注意：使用报修单中的 studentId 获取宿舍信息
                Map<String, Object> dormInfo = dormService.getStudentDormInfo(repairOrder.getStudentId());
                if (dormInfo != null) {
                    repairLocation = repairService.getRepairLocation(dormInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 即使位置获取失败，也不影响详情展示
            }

            // 图片处理：将逗号分隔的字符串转换为列表
            List<String> imagePaths = new ArrayList<>();
            if (repairOrder.getImages() != null && !repairOrder.getImages().isEmpty()) {
                imagePaths = Arrays.asList(repairOrder.getImages().split(","));
            }

            // 添加模型数据
            model.addAttribute("repairOrder", repairOrder);
            model.addAttribute("repairLocation", repairLocation);
            model.addAttribute("imagePaths", imagePaths);

            // 关键：设置返回路径为报修管理列表，覆盖默认的学生主页
            // 配合 repair_detail.html 中的 th:onclick 修改使用
            model.addAttribute("backUrl", "/dorm/repair");

            return "repair_detail";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/dorm/repair";
        }
    }

    // 辅助方法：解析管理员的 manage_building 字段
    private List<String> getBuildingNos(Admin admin) {
        if (admin.getManageBuilding() == null || admin.getManageBuilding().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(admin.getManageBuilding().replace("，", ",").split(","));
    }
}