package org.codeup.statiocore.service;

import org.codeup.statiocore.domain.User;
import org.codeup.statiocore.domain.enums.Role;
import org.codeup.statiocore.web.dto.admin.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAdminService {
    AdminDashboardResponse getDashboard();
    List<User> getAllUsers();
    User updateUserRole(UUID userId, Role role);
    void deactivateUser(UUID userId, User currentUser);
    List<BuildingResponse> getAllBuildings();
    
    // Building CRUD
    BuildingResponse createBuilding(BuildingRequest request);
    BuildingResponse updateBuilding(UUID buildingId, BuildingRequest request);
    void deleteBuilding(UUID buildingId);
    Page<BuildingResponse> getBuildingsPaginated(Pageable pageable);
    
    // Floor CRUD
    List<FloorResponse> getAllFloors();
    FloorResponse createFloor(FloorRequest request);
    FloorResponse updateFloor(UUID floorId, FloorRequest request);
    void deleteFloor(UUID floorId);
    Page<FloorResponse> getFloorsPaginated(Pageable pageable);
    
    // Spot CRUD
    List<SpotResponse> getAllSpots();
    SpotResponse createSpot(SpotRequest request);
    SpotResponse updateSpot(UUID spotId, SpotRequest request);
    void deleteSpot(UUID spotId);
    Page<SpotResponse> getSpotsPaginated(Pageable pageable);
}

