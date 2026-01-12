package org.example.controller;

import org.example.entity.Room;
import org.example.entity.Student;
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

    @Autowired
    private org.example.mapper.UserMapper userMapper;

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
        Integer adminId = 1; // 暂定为 1
        allocationService.assignDorm(studentId, roomId, adminId);
        return "redirect:/admin/allocation/list";
    }

    @GetMapping("/search")
    public String searchStudent(@RequestParam String studentNo,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Student student = userMapper.findStudentByStudentNo(studentNo);
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到学号为 " + studentNo + " 的学生！");
            return "redirect:/dorm_admin_index";
        }
        return "redirect:/admin/allocation/assign?studentId=" + student.getStudentId();
    }

    // ✅ [新增] 4. 跳转到换寝室页面
    @GetMapping("/change")
    public String changePage(@RequestParam Integer studentId, Model model) {
        // 复用获取可用房间的逻辑
        List<Room> rooms = allocationService.getAvailableRooms(studentId);
        model.addAttribute("studentId", studentId);
        model.addAttribute("rooms", rooms);
        return "sys_admin/allocation_change";
    }

    // ✅ [新增] 5. 执行换寝室
    @PostMapping("/doChange")
    public String doChange(@RequestParam Integer studentId,
                           @RequestParam Integer roomId,
                           HttpSession session) {
        Integer adminId = 1; // 暂定为 1
        allocationService.changeDorm(studentId, roomId, adminId);

        // 换寝成功后，建议返回到“我的楼栋房间列表”或者“首页”
        return "redirect:/admin/dorm/rooms"; // 假设这是你的房间列表路径，如果不是请修改
    }
}