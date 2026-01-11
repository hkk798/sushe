package org.example.controller;

import org.example.entity.Student;
import org.example.entity.User;
import org.example.service.StudentService;
import org.example.service.DormService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dorm")
public class DormController {
    
    @Autowired
    private StudentService studentService;
    
    @Autowired
    private DormService dormService;
    
    /**
     * 我的宿舍页面
     */
    @GetMapping("/my_dorm")
    public String myDorm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        
        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
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
            
            // 获取学生的宿舍分配信息
            Map<String, Object> dormInfo = dormService.getStudentDormInfo(currentStudent.getStudentId());
            model.addAttribute("dormInfo", dormInfo);
            
            // 获取其他舍友信息（排除当前用户）
            Map<String, Object> otherRoommatesInfo = null;
            List<Map<String, Object>> otherRoommates = new ArrayList<>();
            
            if (dormInfo != null && dormInfo.get("roomId") != null) {
                Integer roomId = (Integer) dormInfo.get("roomId");
                otherRoommatesInfo = dormService.getRoommatesInfo(roomId, currentStudent.getStudentId());
                otherRoommates = (List<Map<String, Object>>) otherRoommatesInfo.get("roommatesList");
            }
            
            model.addAttribute("otherRoommates", otherRoommates); // 修改为otherRoommates
            
            // 计算入住情况统计
            Map<String, Object> stats = dormService.getDormStats(dormInfo, otherRoommatesInfo);
            model.addAttribute("stats", stats);
            
            // 检查是否有未缴费或警告信息
            String warningMessage = dormService.checkDormStatusWarnings(dormInfo);
            if (warningMessage != null) {
                model.addAttribute("warningMessage", warningMessage);
            }
            
            // 检查是否有宿舍分配
            boolean hasDorm = dormService.hasDormAllocation(currentStudent.getStudentId());
            model.addAttribute("hasDorm", hasDorm);
            
            // 添加学生信息到模型
            Map<String, Object> studentData = studentService.getStudentDashboardData(currentStudent.getStudentId());
            model.addAttribute("student", studentData.get("student"));
            
            return "my_dorm";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "获取宿舍信息失败: " + e.getMessage());
            return "my_dorm";
        }
    }
    
    /**
     * 查看房间报修历史页面
     */
    @GetMapping("/room_repair_history")
    public String roomRepairHistory(HttpSession session, Model model) {
        // 权限验证
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        
        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        
        try {
            // 获取学生的宿舍信息
            Map<String, Object> dormInfo = dormService.getStudentDormInfo(currentStudent.getStudentId());
            if (dormInfo == null) {
                model.addAttribute("errorMessage", "您当前没有宿舍分配信息");
                return "redirect:/my_dorm";
            }
            
            Integer roomId = (Integer) dormInfo.get("roomId");
            if (roomId == null) {
                model.addAttribute("errorMessage", "无法获取房间信息");
                return "redirect:/my_dorm";
            }
            
            // 获取房间报修历史
            Map<String, Object> repairHistory = dormService.getRoomRepairHistory(roomId);
            model.addAttribute("repairHistory", repairHistory);
            model.addAttribute("dormInfo", dormInfo);
            
            return "room_repair_history";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "获取报修历史失败: " + e.getMessage());
            return "room_repair_history";
        }
    }
}