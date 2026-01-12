package org.example.controller;

import com.github.pagehelper.PageInfo; // 👈 必须导入 PageInfo
import org.example.entity.SystemLog;
import org.example.entity.User;
import org.example.service.SystemLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // 👈 导入 RequestParam

@Controller
@RequestMapping("/admin")
public class SystemLogController {

    @Autowired
    private SystemLogService systemLogService;

    /**
     * 查看系统日志页面
     */
    @GetMapping("/system_log")
    public String viewSystemLogs(@RequestParam(defaultValue = "1") Integer page, // 👈 接收页码
                                 @RequestParam(defaultValue = "10") Integer size, // 👈 接收每页数量
                                 HttpSession session,
                                 Model model) {

        // 1. 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        // 2. 获取分页数据 (调用 Service 的分页方法)
        // 注意：这需要您的 SystemLogService 已经实现了 getAllLogs(page, size) 方法
        PageInfo<SystemLog> pageInfo = systemLogService.getAllLogs(page, size);

        // 3. 放入 Model，变量名必须叫 "pageInfo" (对应 HTML 中的 ${pageInfo.list})
        model.addAttribute("pageInfo", pageInfo);

        return "sys_admin/system_log";
    }
}