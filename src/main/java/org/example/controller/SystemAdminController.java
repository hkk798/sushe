package org.example.controller;

import org.example.entity.User;
import org.example.mapper.BuildingMapper;
import org.example.mapper.RepairOrderMapper;
import org.example.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SystemAdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    /**
     * 9.2.5 系统管理员主页
     */
    @GetMapping("/sys_admin_index")
    public String sysAdminIndex(HttpSession session, Model model) {
        // 1. 权限校验 (9.2.5.3 功能 - 访问控制)
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!"system_admin".equals(currentUser.getRole())) {
            // 如果不是系统管理员，重定向到登录页并提示错误
            return "redirect:/login?error=true";
        }

        // 2. 获取全局统计数据 (9.2.5.2 主页界面元素 - 全局统计)
        int totalUsers = userMapper.countAll();
        int totalBuildings = buildingMapper.countAll();
        int totalRepairs = repairOrderMapper.countAll();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalBuildings", totalBuildings);
        model.addAttribute("totalRepairs", totalRepairs);

        // 3. 模拟关键操作日志 (9.2.5.2 主页界面元素 - 关键操作日志 [可选])
        // 实际开发中应从 LogMapper 查询
        List<Map<String, String>> systemLogs = new ArrayList<>();
        Map<String, String> log1 = new HashMap<>();
        log1.put("time", "2023-10-27 10:00:00");
        log1.put("operator", "admin001");
        log1.put("action", "添加了新楼栋：西10");
        systemLogs.add(log1);

        model.addAttribute("systemLogs", systemLogs);

        return "sys_admin_index";
    }
}