package org.example.service.impl;

import org.example.entity.Building;
import org.example.mapper.BuildingMapper;
import org.example.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingServiceImpl implements BuildingService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Override
    public List<Building> getAllBuildings() {
        return buildingMapper.findAll();
    }

    @Override
    public Building getBuildingById(Integer id) {
        return buildingMapper.findById(id);
    }

    @Override
    public void saveBuilding(Building building) {
        if (building.getBuildingId() == null) {
            // ID 为空，说明是新增
            if (building.getRoomCount() == null) building.setRoomCount(0); // 默认0
            buildingMapper.insert(building);
        } else {
            // ID 不为空，说明是更新
            buildingMapper.update(building);
        }
    }

    @Override
    public void deleteBuilding(Integer id) {
        // 实际项目中，这里应该先检查该楼栋下是否有学生或房间，如果有则不允许删除
        buildingMapper.deleteById(id);
    }

    @Override
    public Building getBuildingByNo(String buildingNo) {
        return buildingMapper.findByBuildingNo(buildingNo);
    }
}