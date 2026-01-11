package org.example.controller;

import org.example.entity.RepairOrder;
import org.example.entity.Student;
import org.example.entity.User;
import org.example.service.RepairService;
import org.example.service.DormService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
public class RepairController {
    
    @Autowired
    private RepairService repairService;
    
    @Autowired
    private DormService dormService;
    
    /**
     * 显示提交报修申请页面
     */
    @GetMapping("/repair_submit")
    public String showRepairSubmitPage(HttpSession session, Model model) {
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
                model.addAttribute("errorMessage", "您当前没有宿舍分配信息，无法提交报修申请");
                return "redirect:/my_dorm";
            }
            
            // 获取报修位置信息
            String repairLocation = repairService.getRepairLocation(dormInfo);
            model.addAttribute("repairLocation", repairLocation);
            
            // 获取报修类别列表
            model.addAttribute("repairCategories", repairService.getRepairCategories());
            
            // 修改：从 session 中获取之前的输入数据
            String errorMessage = (String) session.getAttribute("errorMessage");
            String category = (String) session.getAttribute("category");
            String description = (String) session.getAttribute("description");
            
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
                session.removeAttribute("errorMessage"); // 清除 session 中的消息
            }
            if (category != null) {
                model.addAttribute("category", category);
                session.removeAttribute("category");
            }
            if (description != null) {
                model.addAttribute("description", description);
                session.removeAttribute("description");
            }
            
            return "repair_submit";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "加载报修页面失败: " + e.getMessage());
            return "repair_submit";
        }
    }
    
    /**
     * 提交报修申请
     */
    @PostMapping("/repair_submit")
    public String submitRepair(
            @RequestParam String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile[] images,
            HttpSession session,
            RedirectAttributes redirectAttributes) {  // 修改：改为 RedirectAttributes
        
        // 权限验证
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        
        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        
        try {
            // 验证输入（description不再是必须的）
            String validationError = repairService.validateRepairInput(category, description, images);
            if (validationError != null) {
                // 修改：使用 session 临时存储错误信息
                session.setAttribute("errorMessage", validationError);
                session.setAttribute("category", category);
                session.setAttribute("description", description);
                return "redirect:/repair_submit";
            }
            
            // 获取宿舍信息
            Map<String, Object> dormInfo = dormService.getStudentDormInfo(currentStudent.getStudentId());
            if (dormInfo == null) {
                session.setAttribute("errorMessage", "无法获取宿舍信息");
                return "redirect:/my_dorm";
            }
            
            // 保存报修申请
            boolean success = repairService.submitRepair(
                currentStudent.getStudentId(),
                (Integer) dormInfo.get("roomId"),
                category,
                description,
                images
            );
            
            if (success) {
                // 修改：添加成功消息到重定向属性
                redirectAttributes.addFlashAttribute("successMessage", "报修单填写成功！");
                
                // 可选：获取最新的报修单信息显示更多细节
                try {
                    RepairOrder latestRepair = repairService.getLatestRepairByStudentId(currentStudent.getStudentId());
                    if (latestRepair != null) {
                        redirectAttributes.addFlashAttribute("repairTitle", latestRepair.getTitle());
                        redirectAttributes.addFlashAttribute("repairTime", latestRepair.getSubmitTime());
                    }
                } catch (Exception e) {
                    // 不显示详细信息也可以，主要是成功消息
                    e.printStackTrace();
                }
                
                return "redirect:/my_repairs";
            } else {
                session.setAttribute("errorMessage", "提交失败，请重试！");
                session.setAttribute("category", category);
                session.setAttribute("description", description);
                return "redirect:/repair_submit";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "提交报修申请失败: " + e.getMessage());
            session.setAttribute("category", category);
            session.setAttribute("description", description);
            return "redirect:/repair_submit";
        }
    }

    @GetMapping("/my_repairs")
    public String showMyRepairsPage(HttpSession session, Model model,
                                   @RequestParam(value = "status", defaultValue = "all") String status) {
        // 权限验证
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        
        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        
        try {
            // 获取学生的报修历史
            List<RepairOrder> repairs = repairService.getRepairHistoryByStudentId(currentStudent.getStudentId(), status);
            
            // 添加到模型
            model.addAttribute("repairs", repairs);
            model.addAttribute("currentStatus", status);
            model.addAttribute("statusCounts", repairService.getStatusCounts(currentStudent.getStudentId()));
            
            return "my_repairs";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "加载报修记录失败: " + e.getMessage());
            return "my_repairs";
        }
    }
    
    /*撤销报修申请*/
    @PostMapping("/repair/cancel")
    public String cancelRepair(@RequestParam("orderId") Integer orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        // 权限验证
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");
        
        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
            return "redirect:/login";
        }
        
        try {
            // 验证报修单是否属于当前学生
            RepairOrder repairOrder = repairService.getRepairOrderById(orderId);
            if (repairOrder == null || !repairOrder.getStudentId().equals(currentStudent.getStudentId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "报修单不存在或无权操作");
                return "redirect:/my_repairs";
            }
            
            // 验证状态是否为待处理
            if (!repairOrder.isPending()) {
                redirectAttributes.addFlashAttribute("errorMessage", "只能撤销待处理的报修单");
                return "redirect:/my_repairs";
            }
            
            // 执行撤销
            boolean success = repairService.cancelRepair(orderId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "报修单撤销成功");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "撤销失败，请重试");
            }
            
            return "redirect:/my_repairs";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "撤销报修单失败: " + e.getMessage());
            return "redirect:/my_repairs";
        }
    }
    
    /**
     * 显示报修详情页面
     */
    @GetMapping("/repair_detail")
    public String showRepairDetailPage(@RequestParam("orderId") Integer orderId,
                                       HttpSession session,
                                       Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        Student currentStudent = (Student) session.getAttribute("currentStudent");

        if (currentUser == null || currentStudent == null || !"student".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        try {
            // 获取报修单详情
            RepairOrder repairOrder = repairService.getRepairOrderDetail(orderId);

            // 验证报修单是否属于当前学生
            if (repairOrder == null || !repairOrder.getStudentId().equals(currentStudent.getStudentId())) {
                model.addAttribute("errorMessage", "报修单不存在或无权查看");
                return "repair_detail";
            }

            // 关键修复：定义 repairLocation 变量
            String repairLocation;

            try {
                // 获取宿舍信息
                Map<String, Object> dormInfo = dormService.getStudentDormInfo(currentStudent.getStudentId());

                if (dormInfo != null) {
                    // 调用 repairService 获取位置信息
                    repairLocation = repairService.getRepairLocation(dormInfo);
                } else {
                    repairLocation = "未分配宿舍";
                }
            } catch (Exception e) {
                // 如果获取位置失败，使用默认值
                repairLocation = "位置信息获取失败";
                System.err.println("获取报修位置失败: " + e.getMessage());
            }

            // 处理图片路径（如果有）
            List<String> imagePaths = new ArrayList<>();
            if (repairOrder.getImages() != null && !repairOrder.getImages().isEmpty()) {
                imagePaths = Arrays.asList(repairOrder.getImages().split(","));
            }

            // 添加到模型 - 现在 repairLocation 已经定义
            model.addAttribute("repairOrder", repairOrder);
            model.addAttribute("repairLocation", repairLocation);
            model.addAttribute("imagePaths", imagePaths);

            return "repair_detail";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "加载报修详情失败: " + e.getMessage());
            return "repair_detail";
        }
    }
}