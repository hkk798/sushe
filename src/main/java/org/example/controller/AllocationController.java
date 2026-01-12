package org.example.controller;

import org.example.entity.Room;
import org.example.entity.Student;
import org.example.service.AllocationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.example.service.StudentService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/allocation")
public class AllocationController {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private org.example.mapper.UserMapper userMapper;

    @Autowired
    private StudentService studentService;

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
                                RedirectAttributes redirectAttributes) {

        // 1. 查询学生是否存在
        Student student = userMapper.findStudentByStudentNo(studentNo);
        if (student == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到学号为 " + studentNo + " 的学生！");
            return "redirect:/dorm_admin_index";
        }

        // 2. [新增] 检查该学生是否已经分配了宿舍
        boolean hasDorm = studentService.hasDormAllocation(student.getStudentId());
        if (hasDorm) {
            // 如果已分配，添加错误消息并跳回首页，不再进入分配页面
            redirectAttributes.addFlashAttribute("errorMessage",
                    "操作拦截：学生 " + student.getRealName() + " (" + studentNo + ") 已经分配过宿舍了！");
            return "redirect:/dorm_admin_index";
        }

        // 3. 未分配，跳转到分配页面
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
                           HttpSession session,
                           RedirectAttributes redirectAttributes) { // 1. 添加 RedirectAttributes 参数
        Integer adminId = 1; // 暂定为 1
        try {
            allocationService.changeDorm(studentId, roomId, adminId);
            // 成功，重定向到房间列表
            return "redirect:/admin/dorm/rooms";
        } catch (RuntimeException e) {
            // 2. 捕获 Service 抛出的异常（包括“相同房间”、“房间已满”等）
            // 将错误信息放入 Flash 属性中，这样重定向后也能读取到
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            // 3. 发生错误，重定向回“换寝室”页面，让用户重新选择
            return "redirect:/admin/allocation/change?studentId=" + studentId;
        }

    }
}