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
import org.springframework.web.bind.annotation.PathVariable; // 新增
import java.util.List; // 新增
import com.alibaba.excel.EasyExcel;
import org.springframework.web.multipart.MultipartFile;
import org.example.vo.UserImportVO;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
                          @RequestParam(required = false) String gender, // [新增] 接收性别参数
                          // 管理员特有字段
                          @RequestParam(required = false) String adminNo,
                          @RequestParam(required = false) String position,

                          @RequestParam(required = false) String manageBuilding,
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
            user.setPassword(password);
            user.setRealName(realName);
            user.setRole(role);
            user.setEmail(email);
            user.setPhone(phone);
            user.setStatus("active");

            boolean success = false;

            // 3. 根据角色构建关联对象并调用 Service
            if ("student".equals(role)) {
                // === [修改] 学生字段强制校验 ===
                if (studentNo == null || studentNo.isEmpty()) {
                    model.addAttribute("errorMessage", "添加学生必须填写学号");
                    return "sys_admin/user_form";
                }
                // 强制校验专业
                if (major == null || major.isEmpty()) {
                    model.addAttribute("errorMessage", "添加学生必须填写专业");
                    return "sys_admin/user_form";
                }
                // 强制校验班级
                if (className == null || className.isEmpty()) {
                    model.addAttribute("errorMessage", "添加学生必须填写班级");
                    return "sys_admin/user_form";
                }

                Student student = new Student();
                student.setStudentNo(studentNo);
                student.setMajor(major);
                student.setClassName(className);

                // === [修改] 设置动态性别，如果没选默认给 'M' ===
                student.setGender((gender != null && !gender.isEmpty()) ? gender : "M");

                success = userService.register(user, student, null);

            } else if ("building_admin".equals(role) || "system_admin".equals(role)) {
                // ... 管理员逻辑保持不变 ...
                if (adminNo == null || adminNo.isEmpty()) {
                    model.addAttribute("errorMessage", "添加管理员必须填写工号");
                    return "sys_admin/user_form";
                }
                Admin admin = new Admin();
                admin.setAdminNo(adminNo);
                admin.setPosition(position);
                admin.setWorkPhone(phone);

                // [新增] 设置管理楼栋
                // 只有当角色是宿管(building_admin)时，这个字段才有实际意义，但存进去也无妨
                if (manageBuilding != null && !manageBuilding.isEmpty()) {
                    // 统一一下格式，防止中英文逗号混用
                    admin.setManageBuilding(manageBuilding.replace("，", ","));
                }


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


    @GetMapping("/list")
    public String listUsers(@RequestParam(required = false) String role,
                            @RequestParam(required = false) String studentNo, // [新增参数]
                            @RequestParam(required = false) String major,
                            @RequestParam(required = false) String className,
                            @RequestParam(required = false) String msg,
                            Model model, HttpSession session) {

        // 1. 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        // 2. 搜索逻辑 (传入 studentNo)
        List<User> users = userService.searchUsers(role, studentNo, major, className);
        model.addAttribute("users", users);

        // 3. 回显搜索条件
        model.addAttribute("searchRole", role);
        model.addAttribute("searchStudentNo", studentNo); // [新增回显]
        model.addAttribute("searchMajor", major);
        model.addAttribute("searchClassName", className);

        // 4. 消息处理
        if (msg != null && !msg.isEmpty()) {
            model.addAttribute("msg", msg);
        }

        return "sys_admin/user_list";
    }

    @GetMapping("/delete/{userId}")
    public String deleteUser(@PathVariable Integer userId, HttpSession session) {
        // 1. 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        // 2. 执行删除 (这里假设 UserService 有 updateStatus 方法来实现软删除，或者你需要添加物理删除)
        // 建议使用状态更新为 'inactive' 而不是直接物理删除
        userService.updateUserStatus(userId, "inactive");

        return "redirect:/admin/user/list?msg=deleted";
    }


    @PostMapping("/import")
    public String importUserFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // 1. 使用 EasyExcel 同步读取所有数据（适合数据量几千条以内的场景）
            List<UserImportVO> list = EasyExcel.read(file.getInputStream())
                    .head(UserImportVO.class)
                    .sheet()
                    .doReadSync();

            // 2. 调用 Service 处理
            String resultMsg = userService.importUsers(list);

            // 3. 返回结果 (这里使用 URLEncoder 防止中文乱码导致 URL 报错)
            return "redirect:/admin/user/list?msg=" + URLEncoder.encode(resultMsg, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/user/list?error=true&msg=" + URLEncoder.encode("文件解析失败: " + e.getMessage(), StandardCharsets.UTF_8);
        }
    }


    @GetMapping("/restore/{userId}")
    public String restoreUser(@PathVariable Integer userId, HttpSession session) {
        // 1. 权限校验
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"system_admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        // 2. 执行恢复 (将状态改为 active)
        userService.updateUserStatus(userId, "active");

        // 3. 返回提示
        return "redirect:/admin/user/list?msg=" + URLEncoder.encode("用户权限已恢复！", StandardCharsets.UTF_8);
    }
}