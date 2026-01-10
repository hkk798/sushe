package org.example.controller;

import org.example.entity.Admin;
import org.example.entity.Student;
import org.example.entity.User;
import org.example.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/user")
public class UserManageController {

    @Autowired
    private UserService userService;

    /**
     * 跳转到添加用户页面
     */
    @GetMapping("/add")
    public String showAddUserForm(HttpSession session) {
        // 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        return "sys_admin/user_form";
    }

    /**
     * 处理添加用户请求
     */
    @PostMapping("/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String realName,
                          @RequestParam String role,
                          @RequestParam(required = false) String email,
                          @RequestParam(required = false) String phone,
                          // 学生特有字段
                          @RequestParam(required = false) String studentNo,
                          @RequestParam(required = false) String major,
                          @RequestParam(required = false) String className,
                          // 管理员特有字段
                          @RequestParam(required = false) String adminNo,
                          @RequestParam(required = false) String position,
                          Model model, HttpSession session) {

        // 1. 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        try {
            // 2. 构建 User 对象
            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // 实际生产中应加密
            user.setRealName(realName);
            user.setRole(role);
            user.setEmail(email);
            user.setPhone(phone);
            user.setStatus("active");

            boolean success = false;

            // 3. 根据角色构建关联对象并调用 Service
            if ("student".equals(role)) {
                if (studentNo == null || studentNo.isEmpty()) {
                    model.addAttribute("errorMessage", "添加学生必须填写学号");
                    return "sys_admin/user_form";
                }
                Student student = new Student();
                student.setStudentNo(studentNo);
                student.setMajor(major);
                student.setClassName(className);
                student.setGender("M"); // 默认性别，实际表单应提供选择

                success = userService.register(user, student, null);

            } else if ("building_admin".equals(role) || "system_admin".equals(role)) {
                if (adminNo == null || adminNo.isEmpty()) {
                    model.addAttribute("errorMessage", "添加管理员必须填写工号");
                    return "sys_admin/user_form";
                }
                Admin admin = new Admin();
                admin.setAdminNo(adminNo);
                admin.setPosition(position);
                admin.setWorkPhone(phone); // 默认使用手机号作为工作电话

                success = userService.register(user, null, admin);
            }

            if (success) {
                return "redirect:/sys_admin_index?msg=userAdded";
            } else {
                model.addAttribute("errorMessage", "添加失败，用户名可能已存在");
                return "sys_admin/user_form";
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "系统错误: " + e.getMessage());
            return "sys_admin/user_form";
        }
    }
}