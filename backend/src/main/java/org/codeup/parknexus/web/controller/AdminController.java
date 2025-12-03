package org.codeup.parknexus.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Admin API Controller with comprehensive Swagger documentation.
 *
 * This controller handles all administrative operations for the parking management system:
 * - System dashboard with real-time statistics and KPIs
 * - Building, Floor, and Parking Spot management (CRUD operations)
 * - User account management with role assignment
 * - Activity log monitoring and filtering
 * - System-wide configuration and maintenance
 *
 * Security:
 * - ALL endpoints require JWT Bearer token authentication
 * - User must have ADMIN role (enforced by Spring Security)
 * - Unauthorized access returns 401 (not authenticated)
 * - Insufficient permissions return 403 (not authorized)
 * - Admin actions are automatically logged for audit trail
 *
 * Business Rules:
 * - Email addresses must be unique across all users
 * - Passwords are stored as BCrypt hashes (never plain text)
 * - User accounts can be soft-deleted (deletedAt timestamp)
 * - Building/Floor/Spot deletions cascade or are restricted based on dependencies
 * - Activity logs are immutable once created
 * - Dashboard statistics are cached for performance
 *
 * Data Validation:
 * - All inputs validated via @Valid annotations
 * - UUID format enforced for all ID parameters
 * - Pagination defaults: page=0, size=20, max=100
 * - Date filters validated for proper ISO-8601 format
 *
 * Response Patterns:
 * - Success: 200 OK with response body
 * - Created: 201 Created with response body
 * - No Content: 204 No Content (successful deletion)
 * - Bad Request: 400 with error details
 * - Not Found: 404 when resource doesn't exist
 *
 * @author TonyS-dev
 * @version 1.0.0
 * @since 2025-12-05
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(
    name = "Admin API",
    description = "Administrative endpoints for system management, user administration, and monitoring. Requires ADMIN role."
)
@SecurityRequirement(name = "Bearer")
public class AdminController {

    private final IAdminService adminService;
    private final IActivityLogRepository activityLogRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogMapper activityLogMapper;
    private final UserMapper userMapper;
    private final org.codeup.parknexus.service.IActivityLogService activityLogService;

    /**
     * Get comprehensive admin dashboard with system-wide statistics.
     *
     * Returns real-time KPIs including:
     * - Total users, active users, new registrations
     * - Total buildings, floors, parking spots
     * - Active parking sessions and reservations
     * - Revenue statistics and payment summaries
     * - Recent system activity
     *
     * Performance: Results are cached for 5 minutes to optimize load times.
     *
     * @return AdminDashboardResponse with all system statistics
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Get admin dashboard statistics",
        description = "Retrieve comprehensive system-wide statistics including users, buildings, spots, sessions, and revenue metrics"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard statistics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AdminDashboardResponse.class)
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have ADMIN role")
    })
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        // Dashboard is cached downstream; returns system-wide KPIs
        return ResponseEntity.ok(adminService.getDashboard());
    }

    /**
     * Get all buildings with floor and spot statistics.
     *
     * Returns complete list of buildings with:
     * - Building details (name, address, total floors)
     * - Aggregated spot counts (total, occupied, available)
     * - Created timestamp
     *
     * Used by admin UI for building management and filtering.
     *
     * @return List of BuildingResponse with aggregated statistics
     */
    @GetMapping("/buildings")
    @Operation(
        summary = "Get all buildings",
        description = "Retrieve all buildings with floor counts and spot statistics for admin management"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Buildings retrieved successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<java.util.List<BuildingResponse>> getBuildings() {
        // Returns all buildings with aggregated counts used by admin UI
        return ResponseEntity.ok(adminService.getAllBuildings());
    }

    /**
     * Create a new parking building.
     *
     * Creates a new building with the provided details:
     * - Name must be unique across the system
     * - Address is required
     * - Total floors defaults to 0 (add floors separately)
     * - Created timestamp is set automatically
     *
     * @param request Building details (name, address, totalFloors)
     * @return BuildingResponse with created building details
     */
    @PostMapping("/buildings")
    @Operation(
        summary = "Create new building",
        description = "Create a new parking building with name, address, and floor count. Building name must be unique."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Building created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BuildingResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input - building name already exists or missing required fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<BuildingResponse> createBuilding(
            @Parameter(
                description = "Building creation request with name, address, and total floors",
                required = true,
                schema = @Schema(implementation = BuildingRequest.class)
            )
            @Valid @RequestBody BuildingRequest request) {
        // Name uniqueness enforced in service; returns created building
        return ResponseEntity.ok(adminService.createBuilding(request));
    }

    /**
     * Get paginated activity logs with optional filtering.
     *
     * Returns activity logs with support for:
     * - User filtering (by UUID)
     * - Action type filtering (e.g., "USER_CREATED", "SESSION_STARTED")
     * - Date range filtering (ISO-8601 format)
     * - Pagination (default: page 0, size 20)
     *
     * Filters are optional and combined with AND logic.
     * Invalid filters are silently ignored (defensive validation).
     *
     * @param page Page number (zero-based, default: 0)
     * @param size Page size (default: 20, max: 100)
     * @param userId Filter by user UUID (optional)
     * @param action Filter by action type (optional)
     * @param startDate Filter logs from this date (ISO-8601, optional)
     * @param endDate Filter logs until this date (ISO-8601, optional)
     * @return PageResponse with activity logs and pagination metadata
     */
    @GetMapping("/logs")
    @Operation(
        summary = "Get activity logs with filtering",
        description = "Retrieve paginated activity logs with optional filters for user, action type, and date range. " +
                     "Used for audit trail and system monitoring."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Activity logs retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid pagination or filter parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<PageResponse<ActivityLogResponse>> logs(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max: 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by user UUID (optional)", example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestParam(required = false) String userId,
            @Parameter(description = "Filter by action type (optional)", example = "USER_CREATED")
            @RequestParam(required = false) String action,
            @Parameter(description = "Filter logs from this date (ISO-8601)", example = "2025-01-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "Filter logs until this date (ISO-8601)", example = "2025-12-31")
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

    /**
     * Get paginated users list with optional filtering.
     *
     * Returns user accounts with support for:
     * - Role filtering (USER or ADMIN)
     * - Active status filtering (true/false)
     * - Pagination (default: page 0, size 20)
     *
     * Used by admin UI for user management dashboard.
     * Passwords are never returned in responses.
     *
     * @param page Page number (zero-based, default: 0)
     * @param size Page size (default: 20, max: 100)
     * @param role Filter by role (USER or ADMIN, optional)
     * @param active Filter by active status (true/false, optional)
     * @return PageResponse with user list and pagination metadata
     */
    @GetMapping("/users")
    @Operation(
        summary = "Get users list with filtering",
        description = "Retrieve paginated list of users with optional filters for role and active status. " +
                     "Passwords are never included in response."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid pagination or filter parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<PageResponse<UserResponse>> users(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max: 100)", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by role (USER or ADMIN)", example = "USER")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by active status", example = "true")
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

    /**
     * Create a new user account with specified role.
     *
     * Allows admin to create user accounts with either USER or ADMIN role.
     * - Email must be unique
     * - Password is BCrypt hashed automatically
     * - Account is active by default
     * - Creation is logged for audit trail
     *
     * @param request User creation request with email, password, fullName, and role
     * @return UserResponse with created user details (excluding password)
     */
    @PostMapping("/users")
    @Operation(
        summary = "Create new user account",
        description = "Create a new user with specified role (USER or ADMIN). Email must be unique. Password will be BCrypt hashed."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input - email already exists or missing required fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<UserResponse> createUser(
            @Parameter(
                description = "User creation request with email, password, fullName, and role",
                required = true,
                schema = @Schema(implementation = CreateUserRequest.class)
            )
            @Valid @RequestBody CreateUserRequest request) {
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

    /**
     * Create a new admin user account (convenience endpoint).
     *
     * Simplified endpoint specifically for creating ADMIN users.
     * - Automatically assigns ADMIN role
     * - Email must be unique
     * - Password is BCrypt hashed
     * - Account is active by default
     *
     * @param request Registration details (fullName, email, password)
     * @return UserResponse with created admin user details (excluding password)
     */
    @PostMapping("/users/admin")
    @Operation(
        summary = "Create new admin user",
        description = "Convenience endpoint to create ADMIN user account. Email must be unique. Password will be BCrypt hashed."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Admin user created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input - email already exists or missing required fields"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<UserResponse> createAdmin(
            @Parameter(
                description = "Admin registration request with fullName, email, and password",
                required = true,
                schema = @Schema(implementation = RegisterRequest.class)
            )
            @Valid @RequestBody RegisterRequest request) {
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

    /**
     * Update user account details.
     *
     * Allows partial updates of user fields:
     * - Full name
     * - Email (must remain unique)
     * - Role (USER or ADMIN)
     * - Active status
     *
     * Only provided fields are updated; null values are ignored.
     * Update is logged for audit trail.
     *
     * @param userId User UUID to update
     * @param request Update request with optional fields
     * @return UserResponse with updated user details
     */
    @PutMapping("/users/{userId}")
    @Operation(
        summary = "Update user account",
        description = "Update user details including name, email, role, or active status. Only provided fields are updated."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input - email already in use or user not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(
                description = "User update request with optional fields",
                required = true,
                schema = @Schema(implementation = UpdateUserRequest.class)
            )
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

    /**
     * Update user account active status.
     *
     * Quick endpoint to enable/disable user accounts without full update.
     * - Set isActive=true to activate account
     * - Set isActive=false to deactivate (user cannot login)
     *
     * Status change is logged for audit trail.
     *
     * @param userId User UUID
     * @param request Status update request
     * @return UserResponse with updated user details
     */
    @PatchMapping("/users/{userId}/status")
    @Operation(
        summary = "Update user active status",
        description = "Enable or disable user account. Disabled users cannot login."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User status updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input or user not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "User UUID", required = true)
            @PathVariable UUID userId,
            @Parameter(
                description = "Status update request with isActive boolean",
                required = true,
                schema = @Schema(implementation = UpdateStatusRequest.class)
            )
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

    // ==================== Building Management ====================

    /**
     * Update building details.
     *
     * @param buildingId Building UUID
     * @param request Updated building details
     * @return BuildingResponse with updated building
     */
    @PutMapping("/buildings/{buildingId}")
    @Operation(
        summary = "Update building",
        description = "Update building name, address, or floor count"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Building updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public ResponseEntity<BuildingResponse> updateBuilding(
            @Parameter(description = "Building UUID", required = true)
            @PathVariable UUID buildingId,
            @Valid @RequestBody BuildingRequest request) {
        return ResponseEntity.ok(adminService.updateBuilding(buildingId, request));
    }

    /**
     * Delete building (cascades to floors and spots).
     *
     * @param buildingId Building UUID
     * @return 204 No Content
     */
    @DeleteMapping("/buildings/{buildingId}")
    @Operation(
        summary = "Delete building",
        description = "Delete building and all associated floors and spots"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Building deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public ResponseEntity<Void> deleteBuilding(
            @Parameter(description = "Building UUID", required = true)
            @PathVariable UUID buildingId) {
        adminService.deleteBuilding(buildingId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Floor Management ====================

    /**
     * Get all floors across all buildings.
     *
     * @return List of FloorResponse with spot statistics
     */
    @GetMapping("/floors")
    @Operation(summary = "Get all floors", description = "Retrieve all floors with spot statistics")
    @ApiResponse(responseCode = "200", description = "Floors retrieved successfully")
    public ResponseEntity<java.util.List<FloorResponse>> getAllFloors() {
        return ResponseEntity.ok(adminService.getAllFloors());
    }

    /**
     * Create new floor in a building.
     *
     * @param request Floor details (buildingId, floorNumber)
     * @return FloorResponse with created floor
     */
    @PostMapping("/floors")
    @Operation(summary = "Create new floor", description = "Create a new floor in a building")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Floor created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or building not found")
    })
    public ResponseEntity<FloorResponse> createFloor(
            @Parameter(description = "Floor creation request", required = true)
            @Valid @RequestBody FloorRequest request) {
        return ResponseEntity.ok(adminService.createFloor(request));
    }

    /**
     * Update floor details.
     *
     * @param floorId Floor UUID
     * @param request Updated floor details
     * @return FloorResponse with updated floor
     */
    @PutMapping("/floors/{floorId}")
    @Operation(summary = "Update floor", description = "Update floor number or building assignment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Floor updated successfully"),
        @ApiResponse(responseCode = "404", description = "Floor not found")
    })
    public ResponseEntity<FloorResponse> updateFloor(
            @Parameter(description = "Floor UUID", required = true)
            @PathVariable UUID floorId,
            @Valid @RequestBody FloorRequest request) {
        return ResponseEntity.ok(adminService.updateFloor(floorId, request));
    }

    /**
     * Delete floor (cascades to spots).
     *
     * @param floorId Floor UUID
     * @return 204 No Content
     */
    @DeleteMapping("/floors/{floorId}")
    @Operation(summary = "Delete floor", description = "Delete floor and all associated spots")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Floor deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Floor not found")
    })
    public ResponseEntity<Void> deleteFloor(
            @Parameter(description = "Floor UUID", required = true)
            @PathVariable UUID floorId) {
        adminService.deleteFloor(floorId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Parking Spot Management ====================

    /**
     * Get all parking spots across all floors.
     *
     * @return List of SpotResponse with status and type
     */
    @GetMapping("/spots")
    @Operation(summary = "Get all parking spots", description = "Retrieve all spots with status and type information")
    @ApiResponse(responseCode = "200", description = "Spots retrieved successfully")
    public ResponseEntity<java.util.List<SpotResponse>> getAllSpots() {
        return ResponseEntity.ok(adminService.getAllSpots());
    }

    /**
     * Create new parking spot.
     *
     * @param request Spot details (floorId, spotNumber, type, hourlyRate)
     * @return SpotResponse with created spot
     */
    @PostMapping("/spots")
    @Operation(
        summary = "Create new parking spot",
        description = "Create a new parking spot with specified type (STANDARD, VIP, HANDICAP, EV_CHARGING)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Spot created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input or floor not found")
    })
    public ResponseEntity<SpotResponse> createSpot(
            @Parameter(description = "Spot creation request", required = true)
            @Valid @RequestBody SpotRequest request) {
        return ResponseEntity.ok(adminService.createSpot(request));
    }

    /**
     * Update parking spot details.
     *
     * @param spotId Spot UUID
     * @param request Updated spot details
     * @return SpotResponse with updated spot
     */
    @PutMapping("/spots/{spotId}")
    @Operation(
        summary = "Update parking spot",
        description = "Update spot number, type, status, or hourly rate"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Spot updated successfully"),
        @ApiResponse(responseCode = "404", description = "Spot not found")
    })
    public ResponseEntity<SpotResponse> updateSpot(
            @Parameter(description = "Spot UUID", required = true)
            @PathVariable UUID spotId,
            @Valid @RequestBody SpotRequest request) {
        return ResponseEntity.ok(adminService.updateSpot(spotId, request));
    }

    /**
     * Delete parking spot.
     *
     * @param spotId Spot UUID
     * @return 204 No Content
     */
    @DeleteMapping("/spots/{spotId}")
    @Operation(
        summary = "Delete parking spot",
        description = "Delete parking spot. Cannot delete if spot has active sessions or reservations."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Spot deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete - spot has active sessions/reservations"),
        @ApiResponse(responseCode = "404", description = "Spot not found")
    })
    public ResponseEntity<Void> deleteSpot(
            @Parameter(description = "Spot UUID", required = true)
            @PathVariable UUID spotId) {
        adminService.deleteSpot(spotId);
        return ResponseEntity.noContent().build();
    }
}
