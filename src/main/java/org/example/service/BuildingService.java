package org.example.service;

import org.example.entity.Building;
import java.util.List;

public interface BuildingService {
    List<Building> getAllBuildings();
    Building getBuildingById(Integer id);
    void saveBuilding(Building building); // 新增或修改
    void deleteBuilding(Integer id);

    Building getBuildingByNo(String buildingNo);
}