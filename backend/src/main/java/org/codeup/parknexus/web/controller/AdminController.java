package org.codeup.parknexus.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.domain.ActivityLog;
import org.codeup.parknexus.domain.Building;
import org.codeup.parknexus.domain.User;
import org.codeup.parknexus.domain.enums.Role;
import org.codeup.parknexus.exception.BadRequestException;
import org.codeup.parknexus.repository.IActivityLogRepository;
import org.codeup.parknexus.repository.IUserRepository;
import org.codeup.parknexus.service.IAdminService;
import org.codeup.parknexus.web.dto.auth.RegisterRequest;
import org.codeup.parknexus.web.dto.common.PageResponse;
import org.codeup.parknexus.web.dto.admin.*;
import org.codeup.parknexus.web.mapper.ActivityLogMapper;
import org.codeup.parknexus.web.mapper.BuildingMapper;
import org.codeup.parknexus.web.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final IAdminService adminService;
    private final IActivityLogRepository activityLogRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogMapper activityLogMapper;
    private final BuildingMapper buildingMapper;
    private final UserMapper userMapper;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/buildings")
    public ResponseEntity<java.util.List<BuildingResponse>> getBuildings() {
        return ResponseEntity.ok(adminService.getAllBuildings());
    }

    @PostMapping("/buildings")
    public ResponseEntity<BuildingResponse> createBuilding(@Valid @RequestBody BuildingRequest request) {
        Building building = Building.builder().name(request.getName()).address(request.getAddress()).build();
        return ResponseEntity.ok(buildingMapper.toResponse(building));
    }

    @GetMapping("/logs")
    public ResponseEntity<PageResponse<ActivityLogResponse>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> logPage = activityLogRepository.findAll(pageable);

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
        Pageable pageable = PageRequest.of(page, size);

        // Build specification dynamically based on filters
        org.springframework.data.jpa.domain.Specification<org.codeup.parknexus.domain.User> spec =
            org.codeup.parknexus.repository.specification.UserSpecification.withFilters(
                role != null ? org.codeup.parknexus.domain.enums.Role.valueOf(role) : null,
                active
            );

        Page<org.codeup.parknexus.domain.User> userPage = userRepository.findAll(spec, pageable);

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

    @PostMapping("/users/admin")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody RegisterRequest request) {
        // Check if user already exists
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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(admin));
    }
}

