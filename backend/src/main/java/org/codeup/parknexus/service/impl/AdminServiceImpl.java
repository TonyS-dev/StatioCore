package org.codeup.parknexus.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.Role;
import org.codeup.parknexus.domain.enums.SpotStatus;
import org.codeup.parknexus.domain.enums.SessionStatus;
import org.codeup.parknexus.repository.IBuildingRepository;
import org.codeup.parknexus.repository.IFloorRepository;
import org.codeup.parknexus.repository.IParkingSpotRepository;
import org.codeup.parknexus.repository.IUserRepository;
import org.codeup.parknexus.repository.IPaymentRepository;
import org.codeup.parknexus.repository.IParkingSessionRepository;
import org.codeup.parknexus.repository.IReservationRepository;
import org.codeup.parknexus.domain.enums.PaymentStatus;
import org.codeup.parknexus.service.IActivityLogService;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.web.dto.admin.AdminDashboardResponse;
import org.codeup.parknexus.web.dto.admin.BuildingResponse;
import org.codeup.parknexus.web.dto.admin.BuildingRequest;
import org.codeup.parknexus.web.dto.admin.FloorResponse;
import org.codeup.parknexus.web.dto.admin.FloorRequest;
import org.codeup.parknexus.web.dto.admin.SpotResponse;
import org.codeup.parknexus.web.dto.admin.SpotRequest;
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
    private final IFloorRepository floorRepository;
    private final IParkingSpotRepository parkingSpotRepository;
    private final IParkingSessionRepository parkingSessionRepository;
    private final IPaymentRepository paymentRepository;
    private final IReservationRepository reservationRepository;
    private final BuildingMapper buildingMapper;
    private final IActivityLogService activityLogService;

    @Override
    @Cacheable(value = "adminDashboard")
    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long admins = userRepository.findByRole(Role.ADMIN).size();
        long activeUsers = userRepository.countByIsActive(true);
        long totalSpots = parkingSpotRepository.count();
        long occupiedSpots = parkingSpotRepository.countByStatus(SpotStatus.OCCUPIED);
        long availableSpots = parkingSpotRepository.countByStatusAndReservedByIsNull(SpotStatus.AVAILABLE);

        // Count active sessions and reservations
        long activeSessions = parkingSessionRepository.countByStatus(SessionStatus.ACTIVE);
        long totalReservations = reservationRepository.count();
        long totalPayments = paymentRepository.count();

        // Calculate total revenue from successful payments
        BigDecimal totalRevenue = paymentRepository.sumTotalAmountByStatus(PaymentStatus.SUCCESS);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalAdmins(admins)
                .activeUsers(activeUsers)
                .totalSpots(totalSpots)
                .occupiedSpots(occupiedSpots)
                .availableSpots(availableSpots)
                .totalRevenue(totalRevenue)
                .activeSessions(activeSessions)
                .totalReservations(totalReservations)
                .totalPayments(totalPayments)
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
        Role oldRole = user.getRole();
        user.setRole(role);
        user = userRepository.save(user);
        
        // Log role update
        activityLogService.log(user, "USER_ROLE_UPDATED", 
            String.format("Admin updated user role for %s from %s to %s", user.getEmail(), oldRole, role));
        
        return user;
    }

    @Override
    public List<BuildingResponse> getAllBuildings() {
        return buildingRepository.findAll().stream()
            .map(building -> {
                long floorCount = floorRepository.countByBuildingId(building.getId());
                long spotCount = parkingSpotRepository.countByFloorBuildingId(building.getId());
                long occupiedCount = parkingSpotRepository.countByFloorBuildingIdAndStatus(building.getId(), SpotStatus.OCCUPIED);
                long availableCount = parkingSpotRepository.countByFloorBuildingIdAndStatus(building.getId(), SpotStatus.AVAILABLE);

                BuildingResponse response = buildingMapper.toResponse(building);
                response.setTotalFloors((int) floorCount);
                response.setTotalSpots((int) spotCount);
                response.setOccupiedSpots((int) occupiedCount);
                response.setAvailableSpots((int) availableCount);
                return response;
            })
            .toList();
    }

    @Override
    public BuildingResponse createBuilding(BuildingRequest request) {
        org.codeup.parknexus.domain.Building building = org.codeup.parknexus.domain.Building.builder()
            .name(request.getName())
            .address(request.getAddress())
            .createdAt(java.time.OffsetDateTime.now())
            .updatedAt(java.time.OffsetDateTime.now())
            .build();
        
        building = buildingRepository.save(building);
        return buildingMapper.toResponse(building);
    }

    @Override
    public BuildingResponse updateBuilding(UUID buildingId, BuildingRequest request) {
        org.codeup.parknexus.domain.Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Building not found"));
        
        building.setName(request.getName());
        building.setAddress(request.getAddress());
        building.setUpdatedAt(java.time.OffsetDateTime.now());
        building = buildingRepository.save(building);
        
        return buildingMapper.toResponse(building);
    }

    @Override
    public void deleteBuilding(UUID buildingId) {
        org.codeup.parknexus.domain.Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Building not found"));
        buildingRepository.delete(building);
    }

    @Override
    public List<FloorResponse> getAllFloors() {
        return floorRepository.findAll().stream()
            .map(floor -> {
                long spotCount = parkingSpotRepository.countByFloorId(floor.getId());
                return FloorResponse.builder()
                    .id(floor.getId())
                    .buildingId(floor.getBuilding().getId())
                    .buildingName(floor.getBuilding().getName())
                    .floorNumber(floor.getFloorNumber())
                    .spotCount((int) spotCount)
                    .createdAt(floor.getCreatedAt())
                    .build();
            })
            .toList();
    }

    @Override
    public FloorResponse createFloor(FloorRequest request) {
        org.codeup.parknexus.domain.Building building = buildingRepository.findById(request.getBuildingId())
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Building not found"));
        
        org.codeup.parknexus.domain.Floor floor = org.codeup.parknexus.domain.Floor.builder()
            .building(building)
            .floorNumber(request.getFloorNumber())
            .capacity(0)
            .createdAt(java.time.OffsetDateTime.now())
            .updatedAt(java.time.OffsetDateTime.now())
            .build();
        
        floor = floorRepository.save(floor);
        
        return FloorResponse.builder()
            .id(floor.getId())
            .buildingId(floor.getBuilding().getId())
            .buildingName(floor.getBuilding().getName())
            .floorNumber(floor.getFloorNumber())
            .spotCount(0)
            .createdAt(floor.getCreatedAt())
            .build();
    }

    @Override
    public FloorResponse updateFloor(UUID floorId, FloorRequest request) {
        org.codeup.parknexus.domain.Floor floor = floorRepository.findById(floorId)
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Floor not found"));
        
        org.codeup.parknexus.domain.Building building = buildingRepository.findById(request.getBuildingId())
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Building not found"));
        
        floor.setBuilding(building);
        floor.setFloorNumber(request.getFloorNumber());
        floor.setUpdatedAt(java.time.OffsetDateTime.now());
        floor = floorRepository.save(floor);
        
        long spotCount = parkingSpotRepository.countByFloorId(floor.getId());
        
        return FloorResponse.builder()
            .id(floor.getId())
            .buildingId(floor.getBuilding().getId())
            .buildingName(floor.getBuilding().getName())
            .floorNumber(floor.getFloorNumber())
            .spotCount((int) spotCount)
            .createdAt(floor.getCreatedAt())
            .build();
    }

    @Override
    public void deleteFloor(UUID floorId) {
        org.codeup.parknexus.domain.Floor floor = floorRepository.findById(floorId)
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Floor not found"));
        floorRepository.delete(floor);
    }

    @Override
    public List<SpotResponse> getAllSpots() {
        return parkingSpotRepository.findAllWithFloorAndBuilding().stream()
            .map(spot -> SpotResponse.builder()
                .id(spot.getId())
                .floorId(spot.getFloor().getId())
                .floorNumber(spot.getFloor().getFloorNumber())
                .buildingName(spot.getFloor().getBuilding().getName())
                .spotNumber(spot.getSpotNumber())
                .type(spot.getType().name())
                .status(spot.getStatus().name())
                .reservedByUserId(spot.getReservedBy() != null ? spot.getReservedBy().getId() : null)
                .reservedByUserName(spot.getReservedBy() != null ? spot.getReservedBy().getFullName() : null)
                .createdAt(spot.getCreatedAt())
                .updatedAt(spot.getUpdatedAt())
                .build())
            .toList();
    }

    @Override
    public SpotResponse createSpot(SpotRequest request) {
        // Validate RESERVED status requires a user
        if ("RESERVED".equals(request.getStatus()) && request.getReservedByUserId() == null) {
            throw new org.codeup.parknexus.exception.BadRequestException("Reserved spots must have a user assigned");
        }
        
        org.codeup.parknexus.domain.Floor floor = floorRepository.findById(request.getFloorId())
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Floor not found"));
        
        User reservedByUser = null;
        if (request.getReservedByUserId() != null) {
            reservedByUser = userRepository.findById(request.getReservedByUserId())
                .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("User not found"));
        }
        
        org.codeup.parknexus.domain.ParkingSpot spot = org.codeup.parknexus.domain.ParkingSpot.builder()
            .floor(floor)
            .spotNumber(request.getSpotNumber())
            .type(org.codeup.parknexus.domain.enums.SpotType.valueOf(request.getType()))
            .status(org.codeup.parknexus.domain.enums.SpotStatus.valueOf(request.getStatus()))
            .reservedBy(reservedByUser)
            .createdAt(java.time.OffsetDateTime.now())
            .updatedAt(java.time.OffsetDateTime.now())
            .build();
        
        spot = parkingSpotRepository.save(spot);
        
        return SpotResponse.builder()
            .id(spot.getId())
            .floorId(spot.getFloor().getId())
            .floorNumber(spot.getFloor().getFloorNumber())
            .buildingName(spot.getFloor().getBuilding().getName())
            .spotNumber(spot.getSpotNumber())
            .type(spot.getType().name())
            .status(spot.getStatus().name())
            .reservedByUserId(spot.getReservedBy() != null ? spot.getReservedBy().getId() : null)
            .reservedByUserName(spot.getReservedBy() != null ? spot.getReservedBy().getFullName() : null)
            .createdAt(spot.getCreatedAt())
            .updatedAt(spot.getUpdatedAt())
            .build();
    }

    @Override
    public SpotResponse updateSpot(UUID spotId, SpotRequest request) {
        // Validate RESERVED status requires a user
        if ("RESERVED".equals(request.getStatus()) && request.getReservedByUserId() == null) {
            throw new org.codeup.parknexus.exception.BadRequestException("Reserved spots must have a user assigned");
        }
        org.codeup.parknexus.domain.ParkingSpot spot = parkingSpotRepository.findById(spotId)
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Parking spot not found"));
        
        org.codeup.parknexus.domain.Floor floor = floorRepository.findById(request.getFloorId())
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Floor not found"));
        
        User reservedByUser = null;
        if (request.getReservedByUserId() != null) {
            reservedByUser = userRepository.findById(request.getReservedByUserId())
                .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("User not found"));
        }
        
        spot.setFloor(floor);
        spot.setSpotNumber(request.getSpotNumber());
        spot.setType(org.codeup.parknexus.domain.enums.SpotType.valueOf(request.getType()));
        spot.setStatus(org.codeup.parknexus.domain.enums.SpotStatus.valueOf(request.getStatus()));
        spot.setReservedBy(reservedByUser);
        spot.setUpdatedAt(java.time.OffsetDateTime.now());
        spot = parkingSpotRepository.save(spot);
        
        return SpotResponse.builder()
            .id(spot.getId())
            .floorId(spot.getFloor().getId())
            .floorNumber(spot.getFloor().getFloorNumber())
            .buildingName(spot.getFloor().getBuilding().getName())
            .spotNumber(spot.getSpotNumber())
            .type(spot.getType().name())
            .status(spot.getStatus().name())
            .reservedByUserId(spot.getReservedBy() != null ? spot.getReservedBy().getId() : null)
            .reservedByUserName(spot.getReservedBy() != null ? spot.getReservedBy().getFullName() : null)
            .createdAt(spot.getCreatedAt())
            .updatedAt(spot.getUpdatedAt())
            .build();
    }

    @Override
    public void deleteSpot(UUID spotId) {
        org.codeup.parknexus.domain.ParkingSpot spot = parkingSpotRepository.findById(spotId)
            .orElseThrow(() -> new org.codeup.parknexus.exception.BadRequestException("Parking spot not found"));
        parkingSpotRepository.delete(spot);
    }
}
