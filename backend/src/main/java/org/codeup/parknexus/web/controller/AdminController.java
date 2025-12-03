package org.codeup.parknexus.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ActivityLog;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.Role;
import org.codeup.parknexus.exception.BadRequestException;
import org.codeup.parknexus.repository.IActivityLogRepository;
import org.codeup.parknexus.repository.IUserRepository;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.web.dto.auth.RegisterRequest;
import org.codeup.parknexus.web.dto.common.PageResponse;
import org.codeup.parknexus.web.dto.admin.*;
import org.springframework.data.jpa.domain.Specification;
import org.codeup.parknexus.repository.specification.UserSpecification;
import org.codeup.parknexus.web.mapper.ActivityLogMapper;
import org.codeup.parknexus.web.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Admin endpoints for system management: dashboard, buildings, users, and logs.
 * Key rules:
 * - ADMIN role required for all routes under /api/admin
 * - Emails must be unique when creating/updating users
 * - Passwords are stored as BCrypt hashes
 * - Activity logs are recorded for admin actions
 *
 * Keep responses lightweight for dashboards (use counts, not full lists).
 *
 * author: TonyS-dev
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;
    private final IActivityLogRepository activityLogRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogMapper activityLogMapper;
    private final UserMapper userMapper;
    private final org.codeup.parknexus.service.IActivityLogService activityLogService;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        // Dashboard is cached downstream; returns system-wide KPIs
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/buildings")
    public ResponseEntity<java.util.List<BuildingResponse>> getBuildings() {
        // Returns all buildings with aggregated counts used by admin UI
        return ResponseEntity.ok(adminService.getAllBuildings());
    }

    @PostMapping("/buildings")
    public ResponseEntity<BuildingResponse> createBuilding(@Valid @RequestBody BuildingRequest request) {
        // Name uniqueness enforced in service; returns created building
        return ResponseEntity.ok(adminService.createBuilding(request));
    }

    @GetMapping("/logs")
    public ResponseEntity<PageResponse<ActivityLogResponse>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // Filters are optional and validated defensively; returns paginated logs
        Pageable pageable = PageRequest.of(page, size);
        
        // Parse userId if provided
        java.util.UUID userUuid = null;
        if (userId != null && !userId.isEmpty()) {
            try {
                userUuid = java.util.UUID.fromString(userId);
            } catch (IllegalArgumentException e) {
                // Invalid UUID, ignore filter
            }
        }
        
        // Parse dates if provided
        java.time.LocalDate start = null;
        java.time.LocalDate end = null;
        if (startDate != null && !startDate.isEmpty()) {
            try {
                start = java.time.LocalDate.parse(startDate);
            } catch (Exception e) {
                // Invalid date, ignore filter
            }
        }
        if (endDate != null && !endDate.isEmpty()) {
            try {
                end = java.time.LocalDate.parse(endDate);
            } catch (Exception e) {
                // Invalid date, ignore filter
            }
        }
        
        // Build specification with filters
        Specification<ActivityLog> spec =
            org.codeup.parknexus.repository.specification.ActivityLogSpecification.withFilters(
                userUuid, action, start, end
            );
        
        Page<ActivityLog> logPage = activityLogRepository.findAll(spec, pageable);

        PageResponse<ActivityLogResponse> response =
            PageResponse.<ActivityLogResponse>builder()
                .items(activityLogMapper.toResponses(logPage.getContent()))
                .page(page)
                .size(size)
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserResponse>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean active) {
        // Dynamic filters via specification, paginated for admin list
        Pageable pageable = PageRequest.of(page, size);

        // Build specification dynamically based on filters
        Specification<User> spec =
            UserSpecification.withFilters(
                role != null ? Role.valueOf(role) : null,
                active
            );

        Page<User> userPage = userRepository.findAll(spec, pageable);

        PageResponse<UserResponse> response =
            PageResponse.<UserResponse>builder()
                .items(userMapper.toResponses(userPage.getContent()))
                .page(page)
                .size(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // Simple guard against duplicate email; password hashed via encoder
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists with email: " + request.getEmail());
        }

        // Create new user with specified role
        Role userRole = request.getRole() != null ? request.getRole() : Role.USER;
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName() != null ? request.getFullName() : request.getEmail())
                .role(userRole)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();

        user = userRepository.save(user);

        // Log user creation
        activityLogService.log(user, "USER_CREATED", 
            String.format("User created: %s with role %s", user.getEmail(), user.getRole()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(user));
    }

    @PostMapping("/users/admin")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        // Convenience endpoint to create ADMIN users; same uniqueness guard
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists with email: " + request.getEmail());
        }

        // Create new admin user with HASHED password
        User admin = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName() != null ? request.getFullName() : request.getEmail())
                .role(Role.ADMIN)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();

        admin = userRepository.save(admin);

        // Log admin creation
        activityLogService.log(admin, "USER_CREATED", 
            String.format("Admin user created: %s with role %s", admin.getEmail(), admin.getRole()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(admin));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        // Partial update; email uniqueness enforced when changed
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Update fields if provided
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Check if new email is already taken
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        
        user.setUpdatedAt(OffsetDateTime.now());
        user = userRepository.save(user);

        // Log user update
        activityLogService.log(user, "USER_UPDATED", 
            String.format("User updated: %s", user.getEmail()));

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody UpdateStatusRequest request) {
        // Toggle active status; useful for account disable/enable
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setIsActive(request.getIsActive());
        user.setUpdatedAt(OffsetDateTime.now());
        user = userRepository.save(user);

        // Log status change
        activityLogService.log(user, "USER_STATUS_UPDATED", 
            String.format("User %s %s", user.getEmail(), request.getIsActive() ? "activated" : "deactivated"));

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Soft delete - set deletedAt timestamp
        user.setDeletedAt(OffsetDateTime.now());
        user.setIsActive(false);
        user.setUpdatedAt(OffsetDateTime.now());
        userRepository.save(user);

        // Log user deletion
        activityLogService.log(user, "USER_DELETED", 
            String.format("User soft deleted: %s", user.getEmail()));

        return ResponseEntity.noContent().build();
    }

    // Building CRUD endpoints
    @PutMapping("/buildings/{buildingId}")
    public ResponseEntity<BuildingResponse> updateBuilding(
            @PathVariable UUID buildingId,
            @Valid @RequestBody BuildingRequest request) {
        return ResponseEntity.ok(adminService.updateBuilding(buildingId, request));
    }

    @DeleteMapping("/buildings/{buildingId}")
    public ResponseEntity<Void> deleteBuilding(@PathVariable UUID buildingId) {
        adminService.deleteBuilding(buildingId);
        return ResponseEntity.noContent().build();
    }

    // Floor CRUD endpoints
    @GetMapping("/floors")
    public ResponseEntity<java.util.List<FloorResponse>> getAllFloors() {
        return ResponseEntity.ok(adminService.getAllFloors());
    }

    @PostMapping("/floors")
    public ResponseEntity<FloorResponse> createFloor(@Valid @RequestBody FloorRequest request) {
        return ResponseEntity.ok(adminService.createFloor(request));
    }

    @PutMapping("/floors/{floorId}")
    public ResponseEntity<FloorResponse> updateFloor(
            @PathVariable UUID floorId,
            @Valid @RequestBody FloorRequest request) {
        return ResponseEntity.ok(adminService.updateFloor(floorId, request));
    }

    @DeleteMapping("/floors/{floorId}")
    public ResponseEntity<Void> deleteFloor(@PathVariable UUID floorId) {
        adminService.deleteFloor(floorId);
        return ResponseEntity.noContent().build();
    }

    // Spot CRUD endpoints
    @GetMapping("/spots")
    public ResponseEntity<java.util.List<SpotResponse>> getAllSpots() {
        return ResponseEntity.ok(adminService.getAllSpots());
    }

    @PostMapping("/spots")
    public ResponseEntity<SpotResponse> createSpot(@Valid @RequestBody SpotRequest request) {
        return ResponseEntity.ok(adminService.createSpot(request));
    }

    @PutMapping("/spots/{spotId}")
    public ResponseEntity<SpotResponse> updateSpot(
            @PathVariable UUID spotId,
            @Valid @RequestBody SpotRequest request) {
        return ResponseEntity.ok(adminService.updateSpot(spotId, request));
    }

    @DeleteMapping("/spots/{spotId}")
    public ResponseEntity<Void> deleteSpot(@PathVariable UUID spotId) {
        adminService.deleteSpot(spotId);
        return ResponseEntity.noContent().build();
    }
}
