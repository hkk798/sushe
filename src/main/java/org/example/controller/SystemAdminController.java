package org.example.controller;

import org.example.entity.SystemLog;
import org.example.entity.User;
import org.example.mapper.BuildingMapper;
import org.example.mapper.RepairOrderMapper;
import org.example.mapper.UserMapper;
import org.example.service.SystemLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SystemAdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private RepairOrderMapper repairOrderMapper;

    // 【新增】注入日志服务
    @Autowired
    private SystemLogService systemLogService;

    /**
     * 9.2.5 系统管理员主页
     */
    @GetMapping("/sys_admin_index")
    public String sysAdminIndex(HttpSession session, Model model) {
        // 1. 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (!"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login?error=true";
        }

        // 2. 获取全局统计数据
        int totalUsers = userMapper.countAll();
        int totalBuildings = buildingMapper.countAll();
        int totalRepairs = repairOrderMapper.countAll();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalBuildings", totalBuildings);
        model.addAttribute("totalRepairs", totalRepairs);

        // 3. 【修改】获取真实日志数据 (只显示最新的 10 条)
        List<SystemLog> allLogs = systemLogService.getAllLogs();

        // 使用 Stream 流截取前 10 条 (防止首页加载过多数据)
        List<SystemLog> recentLogs = allLogs.stream()
                .limit(10)
                .collect(Collectors.toList());

        model.addAttribute("systemLogs", recentLogs);

        return "sys_admin_index";
    }
}