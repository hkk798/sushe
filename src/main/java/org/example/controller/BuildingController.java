package org.example.controller;

import org.example.entity.Building;
import org.example.service.BuildingService;
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
    public String save(Building building) {
        buildingService.saveBuilding(building);
        return "redirect:/admin/building/list";
    }

    // 4. 删除
    @GetMapping("/delete")
    public String delete(@RequestParam Integer id) {
        buildingService.deleteBuilding(id);
        return "redirect:/admin/building/list";
    }
}