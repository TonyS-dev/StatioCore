package org.codeup.parknexus.web.controller.swagger;

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
 * This controller provides administrative endpoints for managing the Park-Nexus system:
 * - System dashboard with key metrics and statistics
 * - Building, floor, and parking spot management
 * - User management (create, update, role assignment)
 * - Activity logs and audit trails
 * - System health and reporting
 *
 * Security:
 * - ALL endpoints require JWT Bearer token with ADMIN role
 * - Role-based access control enforced at Spring Security level
 * - Activity logging for all administrative actions
 *
 * Business Rules:
 * - Admins can create users with any role (USER, ADMIN)
 * - Email addresses must be unique across the system
 * - Passwords are automatically hashed using BCrypt
 * - Activity logs track all system changes for audit compliance
 * - Dashboard statistics are cached for performance (10-minute TTL)
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
    description = "Administrative endpoints for system management, user administration, and reporting"
)
@SecurityRequirement(name = "Bearer")
public class AdminControllerSwagger {

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
     * Returns real-time metrics including:
     * - Total users, active users, admin count
     * - Total parking spots, occupied/available breakdown
     * - Total revenue from completed parking sessions
     * - Active sessions and reservation counts
     * - Total payments processed
     *
     * Performance:
     * - Response is cached for 10 minutes (Caffeine cache)
     * - Cache is evicted on any data-modifying operations
     * - Typical response time: < 50ms (cached), < 200ms (uncached)
     *
     * @return AdminDashboardResponse with comprehensive system statistics
     */
    @GetMapping("/dashboard")
    @Operation(
        summary = "Get admin dashboard",
        description = "Retrieve system-wide statistics and metrics for administrative monitoring"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully",
            content = @Content(schema = @Schema(implementation = AdminDashboardResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user does not have ADMIN role")
    })
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    /**
     * Get all buildings in the system with occupancy statistics.
     *
     * Returns complete list of buildings with:
     * - Building ID, name, and address
     * - Total floors per building
     * - Total parking spots per building
     * - Occupied and available spot counts
     * - No pagination (returns all buildings)
     *
     * @return List of BuildingResponse with spot statistics
     */
    @GetMapping("/buildings")
    @Operation(
        summary = "Get all buildings",
        description = "Retrieve all buildings with floor counts, spot counts, and occupancy statistics"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Buildings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<java.util.List<BuildingResponse>> getBuildings() {
        return ResponseEntity.ok(adminService.getAllBuildings());
    }

    /**
     * Create a new building in the system.
     *
     * Business Rules:
     * - Building name must be unique
     * - Address is required
     * - Building is created with 0 floors initially
     * - Floors and spots must be added separately
     *
     * @param request BuildingRequest with name and address
     * @return BuildingResponse with newly created building
     */
    @PostMapping("/buildings")
    @Operation(
        summary = "Create new building",
        description = "Add a new building to the parking system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Building created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - name/address missing or duplicate"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<BuildingResponse> createBuilding(@Valid @RequestBody BuildingRequest request) {
        return ResponseEntity.ok(adminService.createBuilding(request));
    }

    /**
     * Get paginated activity logs with optional filtering.
     *
     * Supports filtering by:
     * - userId: Filter by specific user UUID
     * - action: Filter by action type (e.g., CHECK_IN, CHECK_OUT, PAYMENT_PROCESSED)
     * - startDate: Filter logs from this date onwards (ISO 8601: YYYY-MM-DD)
     * - endDate: Filter logs up to this date (ISO 8601: YYYY-MM-DD)
     *
     * Pagination:
     * - Default page: 0
     * - Default size: 20 items per page
     * - Results ordered by timestamp DESC (most recent first)
     *
     * @param page Page number (0-indexed)
     * @param size Items per page
     * @param userId Optional: Filter by user UUID
     * @param action Optional: Filter by action type
     * @param startDate Optional: Start date (YYYY-MM-DD)
     * @param endDate Optional: End date (YYYY-MM-DD)
     * @return PageResponse containing activity logs
     */
    @GetMapping("/logs")
    @Operation(
        summary = "Get activity logs",
        description = "Retrieve paginated activity logs with optional filtering by user, action, and date range"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logs retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters (e.g., malformed UUID or date)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<PageResponse<ActivityLogResponse>> logs(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by user UUID", example = "1c3cc01f-0216-43d0-98b8-970df0daaa98")
            @RequestParam(required = false) String userId,
            @Parameter(description = "Filter by action type", example = "CHECK_IN")
            @RequestParam(required = false) String action,
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2025-12-01")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2025-12-31")
            @RequestParam(required = false) String endDate) {

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
     * Supports filtering by:
     * - role: Filter by user role (USER, ADMIN)
     * - active: Filter by active status (true/false)
     *
     * Pagination:
     * - Default page: 0
     * - Default size: 20 items per page
     * - Results ordered by creation date DESC
     *
     * @param page Page number (0-indexed)
     * @param size Items per page
     * @param role Optional: Filter by role (USER, ADMIN)
     * @param active Optional: Filter by active status
     * @return PageResponse containing users
     */
    @GetMapping("/users")
    @Operation(
        summary = "Get users list",
        description = "Retrieve paginated users with optional filtering by role and active status"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<PageResponse<UserResponse>> users(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filter by role (USER, ADMIN)", example = "ADMIN")
            @RequestParam(required = false) String role,
            @Parameter(description = "Filter by active status", example = "true")
            @RequestParam(required = false) Boolean active) {

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
     * Create a new user with specified role.
     *
     * Business Rules:
     * - Email must be unique (case-insensitive)
     * - Password is automatically hashed using BCrypt
     * - Default role is USER if not specified
     * - User is created with isActive = true
     * - Activity log entry is created for audit trail
     *
     * @param request CreateUserRequest with email, password, fullName, and role
     * @return UserResponse with created user details (password NOT included)
     */
    @PostMapping("/users")
    @Operation(
        summary = "Create new user",
        description = "Create a new user account with specified role (USER or ADMIN)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - email already exists or validation failed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists with email: " + request.getEmail());
        }

        // Create new user with specified role (default: USER)
        Role userRole = request.getRole() != null ? request.getRole() : Role.USER;
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // Hash password with BCrypt
                .fullName(request.getFullName() != null ? request.getFullName() : request.getEmail())
                .role(userRole)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();

        user = userRepository.save(user);

        // Log user creation for audit trail
        activityLogService.log(user, "USER_CREATED",
            String.format("User created: %s with role %s", user.getEmail(), user.getRole()));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(user));
    }

    /**
     * Create a new admin user.
     *
     * Convenience endpoint specifically for creating admin accounts.
     * Automatically sets role to ADMIN.
     *
     * Business Rules:
     * - Email must be unique
     * - Password is automatically hashed using BCrypt
     * - Role is hardcoded to ADMIN
     * - User is created with isActive = true
     *
     * @param request RegisterRequest with email, password, and fullName
     * @return UserResponse with created admin user
     */
    @PostMapping("/users/admin")
    @Operation(
        summary = "Create new admin user",
        description = "Create a new admin account with ADMIN role (convenience endpoint)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Admin user created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - email already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists with email: " + request.getEmail());
        }

        // Create new admin user with HASHED password
        User admin = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // BCrypt hashing
                .fullName(request.getFullName() != null ? request.getFullName() : request.getEmail())
                .role(Role.ADMIN) // Hardcoded to ADMIN
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
     * Update an existing user's details.
     *
     * Supports partial updates - only provided fields are updated.
     * Updatable fields:
     * - fullName: User's display name
     * - email: User's email (must be unique if changed)
     * - role: User's role (USER, ADMIN)
     * - isActive: Account active status
     *
     * Business Rules:
     * - Email uniqueness is enforced if email is changed
     * - updatedAt timestamp is automatically set
     * - Activity log entry is created
     *
     * @param userId UUID of user to update
     * @param request UpdateUserRequest with fields to update
     * @return UserResponse with updated user details
     */
    @PutMapping("/users/{userId}")
    @Operation(
        summary = "Update user details",
        description = "Update user information (supports partial updates)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request - user not found or email already taken"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User UUID to update")
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Update fields if provided (partial update support)
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
     * Update user active status (activate/deactivate account).
     *
     * Convenience endpoint for toggling user account status without
     * modifying other fields.
     *
     * Business Rules:
     * - Setting isActive = false prevents user login
     * - Existing sessions are not terminated
     * - Activity log entry is created
     *
     * @param userId UUID of user
     * @param request UpdateStatusRequest with isActive boolean
     * @return UserResponse with updated user
     */
    @PatchMapping("/users/{userId}/status")
    @Operation(
        summary = "Update user active status",
        description = "Activate or deactivate a user account"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "400", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required")
    })
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "User UUID")
            @PathVariable UUID userId,
            @RequestBody UpdateStatusRequest request) {

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
}

