package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.Role;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.repository.IBuildingRepository;
import org.codeup.parknexus.repository.IFloorRepository;
import org.codeup.parknexus.repository.IParkingSpotRepository;
import org.codeup.parknexus.repository.IUserRepository;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.web.dto.admin.AdminDashboardResponse;
import org.codeup.parknexus.web.dto.admin.BuildingResponse;
import org.codeup.parknexus.web.mapper.BuildingMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements IAdminService {
    private final IUserRepository userRepository;
    private final IBuildingRepository buildingRepository;
    private final IParkingSpotRepository parkingSpotRepository;
    private final IFloorRepository floorRepository;
    private final BuildingMapper buildingMapper;

    @Override
    @Cacheable(value = "adminDashboard")
    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long admins = userRepository.findByRole(Role.ADMIN).size();
        long users = userRepository.findByRole(Role.USER).size();
        long totalSpots = parkingSpotRepository.count();
        long occupiedSpots = parkingSpotRepository.countByStatus(SpotStatus.OCCUPIED);

        // Calculate total revenue from completed sessions (demo: use fixed value for now)
        BigDecimal totalRevenue = BigDecimal.valueOf(12345.50);

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .adminsCount(admins)
                .usersCount(users)
                .totalSpots(totalSpots)
                .occupiedSpots(occupiedSpots)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @CacheEvict(value = "adminDashboard", allEntries = true)
    public User updateUserRole(UUID userId, Role role) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public List<BuildingResponse> getAllBuildings() {
        return buildingRepository.findAll().stream()
            .map(building -> {
                long floorCount = floorRepository.countByBuildingId(building.getId());
                long spotCount = parkingSpotRepository.countByFloorBuildingId(building.getId());

                BuildingResponse response = buildingMapper.toResponse(building);
                response.setTotalFloors((int) floorCount);
                response.setTotalSpots((int) spotCount);
                return response;
            })
            .toList();
    }
}
