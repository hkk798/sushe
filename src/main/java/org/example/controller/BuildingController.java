package org.example.controller;

import org.example.entity.Building;
import org.example.entity.User;
import org.example.service.BuildingService;
import org.example.service.SystemLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private SystemLogService systemLogService;

    // 1. 楼栋列表页
    @GetMapping("/list")
    public String list(Model model) {
        List<Building> buildings = buildingService.getAllBuildings();
        model.addAttribute("buildings", buildings);
        return "sys_admin/building_list"; // 对应 templates/sys_admin/building_list.html
    }

    // 2. 跳转到新增/编辑页面
    @GetMapping("/form")
    public String form(@RequestParam(required = false) Integer id, Model model) {
        Building building;
        if (id != null) {
            building = buildingService.getBuildingById(id);
        } else {
            building = new Building(); // 新增时创建一个空对象
        }
        model.addAttribute("building", building);
        return "sys_admin/building_form";
    }

    // 3. 保存提交 (新增或修改)
    @PostMapping("/save")
    public String save(Building building,
                       HttpSession session,
                       HttpServletRequest request) {

        String actionType = (building.getBuildingId() == null) ? "新增楼栋" : "编辑楼栋";

        buildingService.saveBuilding(building);

        User admin = (User) session.getAttribute("currentUser");
        String operator = (admin != null) ? admin.getUsername() : "未知用户";

        // [新增] 记录日志
        systemLogService.recordLog(
                operator,
                actionType,
                "保存了楼栋信息: " + building.getBuildingName() + " (" + building.getBuildingNo() + ")",
                request.getRemoteAddr()
        );

        return "redirect:/admin/building/list";
    }

    // 4. 删除
    @GetMapping("/delete")
    public String delete(@RequestParam Integer id,
                         HttpSession session,         // <---【添加这个】
                         HttpServletRequest request) {

        Building b = buildingService.getBuildingById(id);
        String buildingName = (b != null) ? b.getBuildingName() : "ID:" + id;
        buildingService.deleteBuilding(id);


        // [新增] 记录日志
        User admin = (User) session.getAttribute("currentUser");
        String operator = (admin != null) ? admin.getUsername() : "未知用户";

        systemLogService.recordLog(
                operator,
                "删除楼栋",
                "删除了楼栋: " + buildingName,
                request.getRemoteAddr()
        );

        return "redirect:/admin/building/list";
    }
}