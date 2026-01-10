package org.example.controller;

import org.example.entity.SystemLog;
import org.example.entity.User;
import org.example.service.SystemLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 查看系统日志页面
     */
    @GetMapping("/system_log")
    public String viewSystemLogs(HttpSession session, Model model) {
        // 1. 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        // 2. 获取真实数据库日志
        List<SystemLog> logs = systemLogService.getAllLogs();
        model.addAttribute("logs", logs);

        return "sys_admin/system_log";
    }
}