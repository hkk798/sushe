package org.example.controller;

import org.example.entity.Student;
import org.example.entity.User;
import org.example.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    /**
     * 学生主页
     */
    @GetMapping("/student_index")
    public String studentIndex(HttpSession session, Model model) {
        // 检查用户是否登录
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // 检查角色是否为学生
        if (!"student".equals(currentUser.getRole())) {
            return "redirect:/login?error=true";
        }
        
        // 获取学生信息
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        if (currentStudent == null) {
            return "redirect:/login";
        }
        
        try {
            // 验证访问权限
            boolean hasAccess = studentService.validateStudentAccess(
                currentUser.getUserId(), currentStudent.getStudentId());
            
            if (!hasAccess) {
                session.invalidate();
                return "redirect:/login?error=true";
            }
            
            // 获取学生主页数据
            Map<String, Object> dashboardData = studentService.getStudentDashboardData(
                currentStudent.getStudentId());
            
            // 添加数据到模型
            model.addAttribute("student", dashboardData.get("student"));
            model.addAttribute("dormInfo", dashboardData.get("dormInfo"));
            model.addAttribute("roommates", dashboardData.get("roommates"));
            model.addAttribute("processingRepairsCount", dashboardData.get("processingRepairsCount"));
            model.addAttribute("stats", dashboardData.get("stats"));
            
            // 检查是否有宿舍分配
            boolean hasDorm = studentService.hasDormAllocation(currentStudent.getStudentId());
            model.addAttribute("hasDorm", hasDorm);
            
            if (!hasDorm) {
                model.addAttribute("warningMessage", "您当前没有宿舍分配，请联系管理员。");
            }
            
            return "login/student_index";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "系统错误: " + e.getMessage());
            return "login/student_index";
        }
    }
    
    /**
     * 学生个人信息页面
     */
    @GetMapping("/student_info")
    public String studentInfo(HttpSession session, Model model) {
        // 权限验证
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        
        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        
        try {
            // 重新获取最新的学生信息
            Map<String, Object> studentData = studentService.getStudentDashboardData(
                currentStudent.getStudentId());
            
            model.addAttribute("student", studentData.get("student"));
            model.addAttribute("dormInfo", studentData.get("dormInfo"));
            
            // 获取宿舍历史
            var dormHistory = studentService.getDormHistory(currentStudent.getStudentId());
            model.addAttribute("dormHistory", dormHistory);
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "获取信息失败: " + e.getMessage());
        }
        
        return "student_info";
    }
    
    /**
     * 我的宿舍页面
     */
    @GetMapping("/my_dorm")
    public String myDorm(HttpSession session, Model model) {
        // 权限验证
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        
        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        
        try {
            // 获取宿舍信息
            Map<String, Object> studentData = studentService.getStudentDashboardData(
                currentStudent.getStudentId());
            
            model.addAttribute("student", studentData.get("student"));
            model.addAttribute("dormInfo", studentData.get("dormInfo"));
            model.addAttribute("roommates", studentData.get("roommates"));
            
            if (studentData.get("dormInfo") == null) {
                model.addAttribute("warningMessage", "您当前没有宿舍分配信息。");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "获取宿舍信息失败: " + e.getMessage());
        }
        
        return "my_dorm";
    }
}