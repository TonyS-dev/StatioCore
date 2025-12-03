package org.codeup.parknexus.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.codeup.parknexus.service.IAuthService;
import org.codeup.parknexus.web.dto.auth.AuthResponse;
import org.codeup.parknexus.web.dto.auth.LoginRequest;
import org.codeup.parknexus.web.dto.auth.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication API Controller with comprehensive Swagger documentation.
 *
 * This controller handles public authentication endpoints for user registration and login:
 * - User registration with automatic role assignment (USER by default)
 * - User authentication with JWT token generation
 * - Password encryption using BCrypt hashing algorithm
 *
 * Security:
 * - These are public endpoints (no authentication required)
 * - Passwords are never returned in responses
 * - Passwords are stored as BCrypt hashes (60 characters)
 * - JWT tokens contain: userId, email, role claims
 * - JWT tokens are signed with HS512 algorithm
 *
 * Business Rules:
 * - Email addresses must be unique across the system
 * - New users are automatically assigned USER role (not ADMIN)
 * - Inactive accounts cannot login
 * - Failed login attempts do not reveal if email exists (security)
 * - Passwords must meet minimum length requirements (validated by DTO)
 *
 * Response Format:
 * - Success: Returns JWT token + user details (without password)
 * - Failure: Returns appropriate HTTP status with error message
 *
 * @author TonyS-dev
 * @version 1.0.0
 * @since 2025-12-05
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication API",
    description = "Public endpoints for user registration and authentication"
)
public class AuthController {

    private final IAuthService authService;

    /**
     * Register a new user account with automatic USER role assignment.
     *
     * Creates a new user account with the following defaults:
     * - Role: USER (not ADMIN)
     * - Status: Active
     * - Password: BCrypt hashed (60 chars)
     * - Created timestamp: Current time
     *
     * The endpoint automatically generates a JWT token upon successful registration,
     * allowing immediate authentication without requiring a separate login call.
     *
     * @param request Registration details including fullName, email, and password
     * @return AuthResponse containing JWT token and user details (excluding password)
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new user account",
        description = "Create a new user account with USER role and return authentication token. " +
                     "Email must be unique. Password will be encrypted using BCrypt."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "User registered successfully, JWT token returned",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - email already exists, invalid email format, or password too short",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during registration process",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(
                description = "Registration request with fullName, email, and password",
                required = true,
                schema = @Schema(implementation = RegisterRequest.class)
            )
            @Valid @RequestBody RegisterRequest request) {
        // Registration returns token; email uniqueness enforced downstream
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticate user and generate JWT token.
     *
     * Validates user credentials and generates a signed JWT token containing:
     * - User ID (UUID)
     * - Email address
     * - User role (USER or ADMIN)
     * - Token expiration time
     *
     * Security features:
     * - Password comparison using BCrypt secure hashing
     * - Inactive accounts are rejected
     * - Failed attempts do not reveal if email exists
     * - Rate limiting should be implemented at infrastructure level
     *
     * @param request Login credentials (email and password)
     * @return AuthResponse containing JWT token and user details (excluding password)
     */
    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user and get JWT token",
        description = "Validate user credentials and return JWT token for subsequent authenticated requests. " +
                     "Token must be included in Authorization header as 'Bearer {token}' for protected endpoints."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Authentication successful, JWT token returned",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication failed - invalid credentials or inactive account",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - missing email or password",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during authentication process",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(
                description = "Login credentials with email and password",
                required = true,
                schema = @Schema(implementation = LoginRequest.class)
            )
            @Valid @RequestBody LoginRequest request) {
        // Login issues JWT; ensure inactive accounts are rejected in service
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
