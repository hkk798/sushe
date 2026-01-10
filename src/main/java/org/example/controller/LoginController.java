package org.example.controller;

import org.example.entity.User;
import org.example.entity.Student;
import org.example.entity.Admin;
import org.example.service.UserService;
import org.example.service.SystemLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    
    @Autowired
    private UserService userService;

    @Autowired
    private SystemLogService systemLogService;
    
    // 存储验证码和token（用于忘记密码功能）
    private Map<String, String> verificationCodes = new HashMap<>();
    private Map<String, String> resetTokens = new HashMap<>();
    
    // 显示登录页面
    @GetMapping({"/", "/login"})
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "msg", required = false) String msg,
                                Model model) {
        // 处理错误消息
        if ("true".equals(error)) {
            model.addAttribute("errorMessage", "登录失败！用户名或密码错误。");
        }
        
        // 处理成功消息
        if (msg != null) {
            switch (msg) {
                case "passwordChanged":
                    model.addAttribute("successMessage", "密码修改成功，请重新登录！");
                    break;
                case "registered":
                    model.addAttribute("successMessage", "注册成功，请直接登录！");
                    break;
                case "logout":
                    model.addAttribute("successMessage", "您已安全退出登录。");
                    break;
            }
        }
        
        return "login"; // login.html
    }
    
    // 处理登录请求
    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam String role,
                       @RequestParam(required = false) Boolean remember,
                       HttpSession session,
                       Model model,
                       HttpServletRequest request) {
        
        // 基本验证

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password) || !StringUtils.hasText(role)) {
            model.addAttribute("errorMessage", "用户名、密码和角色不能为空！");
            return "login";
        }
        
        // 调用Service进行登录验证
        Map<String, Object> loginResult = userService.login(username, password, role);
        if (loginResult == null) {
            model.addAttribute("errorMessage", "用户名或密码错误，或账户不可用。");

            model.addAttribute("inputUsername", username);
            model.addAttribute("inputRole", role);

            return "login";
        }
        
        // 登录成功，设置session
        User user = (User) loginResult.get("user");
        session.setAttribute("currentUser", user);
        session.setAttribute("userRole", role);


        // [新增] 记录登录日志 ------------------------------------------
        systemLogService.recordLog(
                user.getUsername() + "(" + user.getRealName() + ")", // 操作人
                "用户登录",                                           // 动作
                "用户成功登录系统，角色: " + role,                      // 详情
                request.getRemoteAddr()                              // IP地址
        );
        // -----------------------------------------------------------

        // 根据角色设置详细信息到session
        if ("student".equals(role)) {
            Student student = (Student) loginResult.get("student");
            session.setAttribute("currentStudent", student);
        } else if ("building_admin".equals(role) || "system_admin".equals(role)) {
            Admin admin = (Admin) loginResult.get("admin");
            session.setAttribute("currentAdmin", admin);
        }
        
        // 记住我功能
        if (remember != null && remember) {
            session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 7天
        } else {
            session.setMaxInactiveInterval(60 * 60); // 1小时
        }
        
        // 根据角色跳转到不同页面
        if ("student".equals(role)) {
            return "redirect:/student_index";
        } else if ("building_admin".equals(role)) {
            return "redirect:/dorm_admin_index";
        } else if ("system_admin".equals(role)) {
            return "redirect:/sys_admin_index";
        }
        
        return "redirect:/";
    }
    
    // 退出登录
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?msg=logout";
    }
    
    // 显示修改密码页面
    @GetMapping("/change_password")
    public String showChangePasswordPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        if(user == null)
            return "redirect:/login";

        return "login/change_password";
    }
    
    // 处理修改密码请求
    @PostMapping("/change_password")
    public String changePassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            return "redirect:/login";
        }
        
        // 使用Service进行验证（后端验证）
        String validationError = userService.validatePasswordChange(oldPassword, newPassword, confirmPassword);
        if (validationError != null) {
            model.addAttribute("errorMessage", validationError);
            return "login/change_password";
        }
        
        // 调用Service修改密码
        boolean success = userService.changePassword(user.getUserId(), oldPassword, newPassword);
        if (success) {
            // 修改成功后退出登录
            session.invalidate();
            return "redirect:/login?msg=passwordChanged";
        } else {
            model.addAttribute("errorMessage", "旧密码错误或修改失败！");
            return "login/change_password";
        }
    }
    
    // 显示忘记密码页面
    @GetMapping("/forgot_password")
    public String showForgotPasswordPage(Model model) {
        model.addAttribute("step", 1);
        return "login/forgot_password";
    }

    @PostMapping("/forgot_password/verify")
    public String verifyIdentity(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String role,
                                @RequestParam String captcha,
                                HttpServletRequest request,
                                Model model) {
        
        // 验证验证码
        HttpSession session = request.getSession();
        String sessionCaptcha = (String) session.getAttribute("captcha");
        
        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(captcha)) {
            model.addAttribute("step", 1);
            model.addAttribute("errorMessage", "验证码错误");
            return "login/forgot_password";
        }
        
        // 验证用户信息（这里简化处理）
        // 实际应该查询数据库验证用户名、邮箱和角色是否匹配
        
        // 生成验证码并发送邮件（模拟）
        String verificationCode = generateVerificationCode();
        String token = generateToken();
        
        // 存储验证码和token（模拟发送邮件）
        verificationCodes.put(token, verificationCode);
        resetTokens.put(token, username);
        
        // 这里模拟发送邮件
        System.out.println("验证码: " + verificationCode + " (已发送到: " + email + ")");
        System.out.println("Token: " + token);
        
        // 设置模型属性
        model.addAttribute("step", 2);
        model.addAttribute("token", token);
        model.addAttribute("successMessage", "验证码已发送到您的邮箱，请查收");
        
        return "login/forgot_password";
    }
    
    // 处理第二步：重置密码
    @PostMapping("/forgot_password/reset")
    public String resetPassword(@RequestParam String token,
                               @RequestParam String emailCode,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               Model model) {
        
        // 验证token
        if (!resetTokens.containsKey(token)) {
            model.addAttribute("step", 1);
            model.addAttribute("errorMessage", "链接已过期，请重新申请");
            return "login/forgot_password";
        }
        
        // 验证验证码
        String storedCode = verificationCodes.get(token);
        if (storedCode == null || !storedCode.equals(emailCode)) {
            model.addAttribute("step", 2);
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "验证码错误");
            return "login/forgot_password";
        }
        
        // 验证密码
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("step", 2);
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "两次输入的密码不一致");
            return "login/forgot_password";
        }
        
        if (newPassword.length() < 6) {
            model.addAttribute("step", 2);
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "密码长度不能少于6位");
            return "login/forgot_password";
        }
        
        // 获取用户名并更新密码（这里需要调用Service）
        String username = resetTokens.get(token);
        
        try {
            // 这里应该调用UserService更新密码
            // userService.updatePassword(username, newPassword);
            System.out.println("为用户 " + username + " 重置密码为: " + newPassword);
            
            // 清理验证数据
            verificationCodes.remove(token);
            resetTokens.remove(token);
            
            // 跳转到第三步：完成
            model.addAttribute("step", 3);
            model.addAttribute("successMessage", "密码重置成功");
            
        } catch (Exception e) {
            model.addAttribute("step", 2);
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "重置密码失败，请重试");
        }
        
        return "login/forgot_password";
    }
    
    // 生成6位验证码
    private String generateVerificationCode() {
        return "123456";
    }
    
    // 生成token
    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}