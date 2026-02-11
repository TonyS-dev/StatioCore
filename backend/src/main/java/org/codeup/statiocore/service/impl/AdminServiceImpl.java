package org.codeup.statiocore.service.impl;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.codeup.statiocore.domain.Building;
import org.codeup.statiocore.domain.Floor;
import org.codeup.statiocore.domain.ParkingSpot;
import org.codeup.statiocore.domain.User;
import org.codeup.statiocore.domain.enums.PaymentStatus;
import org.codeup.statiocore.domain.enums.Role;
import org.codeup.statiocore.domain.enums.SpotStatus;
import org.codeup.statiocore.domain.enums.SpotType;
import org.codeup.statiocore.domain.enums.SessionStatus;
import org.codeup.statiocore.exception.BadRequestException;
import org.codeup.statiocore.repository.IBuildingRepository;
import org.codeup.statiocore.repository.IFloorRepository;
import org.codeup.statiocore.repository.IParkingSpotRepository;
import org.codeup.statiocore.repository.IUserRepository;
import org.codeup.statiocore.repository.IPaymentRepository;
import org.codeup.statiocore.repository.IParkingSessionRepository;
import org.codeup.statiocore.repository.IReservationRepository;
import org.codeup.statiocore.service.IActivityLogService;
import org.codeup.statiocore.service.IAdminService;
import org.codeup.statiocore.web.dto.admin.AdminDashboardResponse;
import org.codeup.statiocore.web.dto.admin.BuildingRequest;
import org.codeup.statiocore.web.dto.admin.BuildingResponse;
import org.codeup.statiocore.web.dto.admin.FloorRequest;
import org.codeup.statiocore.web.dto.admin.FloorResponse;
import org.codeup.statiocore.web.dto.admin.SpotRequest;
import org.codeup.statiocore.web.dto.admin.SpotResponse;
import org.codeup.statiocore.web.mapper.BuildingMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

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
    public void deactivateUser(UUID userId, User currentUser) {
        if (currentUser.getId().equals(userId) && currentUser.getRole() == Role.ADMIN) {
            throw new BadRequestException("An admin cannot deactivate their own account.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);

        activityLogService.log(currentUser, "USER_DEACTIVATED",
                String.format("Admin %s deactivated user %s", currentUser.getEmail(), user.getEmail()));
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
        Building building = Building.builder()
            .name(request.getName())
            .address(request.getAddress())
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .build();
        
        building = buildingRepository.save(building);
        return buildingMapper.toResponse(building);
    }

    @Override
    public BuildingResponse updateBuilding(UUID buildingId, BuildingRequest request) {
        Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new BadRequestException("Building not found"));
        
        building.setName(request.getName());
        building.setAddress(request.getAddress());
        building.setUpdatedAt(OffsetDateTime.now());
        building = buildingRepository.save(building);
        
        return buildingMapper.toResponse(building);
    }

    @Override
    public void deleteBuilding(UUID buildingId) {
        Building building = buildingRepository.findById(buildingId)
            .orElseThrow(() -> new BadRequestException("Building not found"));
        buildingRepository.delete(building);
    }

    @Override
    public Page<BuildingResponse> getBuildingsPaginated(Pageable pageable) {
        Page<Building> buildingsPage = buildingRepository.findAll(pageable);
        
        List<BuildingResponse> content = buildingsPage.getContent().stream()
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
        
        return new PageImpl<>(content, pageable, buildingsPage.getTotalElements());
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
        Building building = buildingRepository.findById(request.getBuildingId())
            .orElseThrow(() -> new BadRequestException("Building not found"));
        
        Floor floor = Floor.builder()
            .building(building)
            .floorNumber(request.getFloorNumber())
            .capacity(0)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
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
        Floor floor = floorRepository.findById(floorId)
            .orElseThrow(() -> new BadRequestException("Floor not found"));
        
        Building building = buildingRepository.findById(request.getBuildingId())
            .orElseThrow(() -> new BadRequestException("Building not found"));
        
        floor.setBuilding(building);
        floor.setFloorNumber(request.getFloorNumber());
        floor.setUpdatedAt(OffsetDateTime.now());
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
        Floor floor = floorRepository.findById(floorId)
            .orElseThrow(() -> new BadRequestException("Floor not found"));
        floorRepository.delete(floor);
    }

    @Override
    public Page<FloorResponse> getFloorsPaginated(Pageable pageable) {
        // Get all floors with eager loading
        List<Floor> allFloors = floorRepository.findAll();
        
        // Convert to FloorResponse and group by building
        Map<String, List<FloorResponse>> floorsByBuilding = allFloors.stream()
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
            .collect(Collectors.groupingBy(
                FloorResponse::getBuildingName,
                LinkedHashMap::new,
                Collectors.toList()
            ));
        
        // Sort buildings by their most recent floor creation date (DESC)
        List<String> buildingNamesOrdered = floorsByBuilding.entrySet().stream()
            .sorted((a, b) -> {
                OffsetDateTime maxA = a.getValue().stream()
                    .map(FloorResponse::getCreatedAt)
                    .max(OffsetDateTime::compareTo)
                    .orElse(OffsetDateTime.now());
                OffsetDateTime maxB = b.getValue().stream()
                    .map(FloorResponse::getCreatedAt)
                    .max(OffsetDateTime::compareTo)
                    .orElse(OffsetDateTime.now());
                return maxB.compareTo(maxA); // DESC
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // Flatten: for each building (in order), add its floors sorted by floor number
        List<FloorResponse> sortedFloors = buildingNamesOrdered.stream()
            .flatMap(buildingName -> floorsByBuilding.get(buildingName).stream()
                .sorted(Comparator.comparingInt(FloorResponse::getFloorNumber)))
            .collect(Collectors.toList());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sortedFloors.size());
        
        List<FloorResponse> pageContent = sortedFloors.subList(start, Math.max(start, end));
        return new PageImpl<>(pageContent, pageable, sortedFloors.size());
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
            throw new BadRequestException("Reserved spots must have a user assigned");
        }
        
        Floor floor = floorRepository.findById(request.getFloorId())
            .orElseThrow(() -> new BadRequestException("Floor not found"));
        
        User reservedByUser = null;
        if (request.getReservedByUserId() != null) {
            reservedByUser = userRepository.findById(request.getReservedByUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        }
        
        ParkingSpot spot = ParkingSpot.builder()
            .floor(floor)
            .spotNumber(request.getSpotNumber())
            .type(SpotType.valueOf(request.getType()))
            .status(SpotStatus.valueOf(request.getStatus()))
            .reservedBy(reservedByUser)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
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
            throw new BadRequestException("Reserved spots must have a user assigned");
        }
        ParkingSpot spot = parkingSpotRepository.findById(spotId)
            .orElseThrow(() -> new BadRequestException("Parking spot not found"));
        
        Floor floor = floorRepository.findById(request.getFloorId())
            .orElseThrow(() -> new BadRequestException("Floor not found"));
        
        User reservedByUser = null;
        if (request.getReservedByUserId() != null) {
            reservedByUser = userRepository.findById(request.getReservedByUserId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        }
        
        spot.setFloor(floor);
        spot.setSpotNumber(request.getSpotNumber());
        spot.setType(SpotType.valueOf(request.getType()));
        spot.setStatus(SpotStatus.valueOf(request.getStatus()));
        spot.setReservedBy(reservedByUser);
        spot.setUpdatedAt(OffsetDateTime.now());
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
        ParkingSpot spot = parkingSpotRepository.findById(spotId)
            .orElseThrow(() -> new BadRequestException("Parking spot not found"));
        parkingSpotRepository.delete(spot);
    }

    @Override
    public Page<SpotResponse> getSpotsPaginated(Pageable pageable) {
        Page<ParkingSpot> spotsPage = parkingSpotRepository.findAllWithFloorAndBuilding(pageable);
        
        List<SpotResponse> content = spotsPage.getContent().stream()
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
        
        return new PageImpl<>(content, pageable, spotsPage.getTotalElements());
    }
}
