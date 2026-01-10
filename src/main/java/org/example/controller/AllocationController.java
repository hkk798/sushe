package org.example.controller;

import org.example.entity.Room;
import org.example.entity.Student;
//import org.example.entity.User;
import org.example.service.AllocationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/allocation")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    // 1. 待分配列表
    @GetMapping("/list")
    public String list(Model model) {
        List<Student> students = allocationService.getUnallocatedStudents();
        model.addAttribute("students", students);
        return "sys_admin/allocation_list";
    }

    // 2. 调转分配页面
    @GetMapping("/assign")
    public String assignPage(@RequestParam Integer studentId, Model model) {
        List<Room> rooms = allocationService.getAvailableRooms(studentId);
        model.addAttribute("studentId", studentId);
        model.addAttribute("rooms", rooms);
        return "sys_admin/allocation_form";
    }

    // 3. 执行分配
    @PostMapping("/doAssign")
    public String doAssign(@RequestParam Integer studentId,
                           @RequestParam Integer roomId,
                           HttpSession session) {

        // 尝试从 Session 获取当前登录的管理员信息
        // (假设登录时存了 User 对象，且 User 对象关联了 Admin 表的 ID)
        // ⚠️ 临时方案：如果 Session 里拿不到 adminId，我们先硬编码为 1，防止报错
        Integer adminId = 1;

        // User currentUser = (User) session.getAttribute("currentUser");
        // if (currentUser != null) { ... 获取 adminId ... }

        allocationService.assignDorm(studentId, roomId, adminId);
        return "redirect:/admin/allocation/list";
    }
}