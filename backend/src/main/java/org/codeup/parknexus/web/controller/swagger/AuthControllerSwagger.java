package org.codeup.parknexus.web.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
 * This controller handles user authentication and registration for the Park-Nexus system:
 * - User registration (create new USER account)
 * - User login (authenticate and obtain JWT token)
 * - No authentication required for these endpoints (public access)
 *
 * Security & Token Management:
 * - Passwords are hashed using BCrypt before storage
 * - JWT tokens are issued upon successful login
 * - Token expiration: 24 hours (configurable in application.yml)
 * - Tokens include: userId, email, role (USER/ADMIN)
 * - Token structure: Header.Payload.Signature (HS512 algorithm)
 *
 * Business Rules:
 * - Email addresses must be unique (case-insensitive)
 * - Passwords must meet minimum requirements (see validation)
 * - New users are created with role = USER by default
 * - New users are automatically activated (isActive = true)
 * - Failed login attempts are logged for security monitoring
 *
 * Rate Limiting:
 * - Login endpoint: 5 attempts per minute per IP (recommended)
 * - Register endpoint: 3 registrations per hour per IP (recommended)
 * - Implement rate limiting at reverse proxy level (Nginx/Cloudflare)
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
    description = "Public endpoints for user registration and login. No authentication required."
)
public class AuthControllerSwagger {

    private final IAuthService authService;

    /**
     * Register a new user account.
     *
     * Creates a new user with role = USER and returns a JWT token for immediate login.
     *
     * Process Flow:
     * 1. Validate request (email format, password strength)
     * 2. Check if email already exists
     * 3. Hash password using BCrypt (strength factor: 10)
     * 4. Create user with role = USER, isActive = true
     * 5. Generate JWT token with user claims
     * 6. Log registration activity
     * 7. Return token and user details
     *
     * Validation Rules:
     * - Email: Must be valid format (RFC 5322)
     * - Password: Minimum 8 characters (recommended: 12+)
     * - Password: Must contain letters and numbers (recommended)
     * - Full Name: Optional, defaults to email if not provided
     *
     * Security Considerations:
     * - Password is NEVER stored in plain text
     * - Password is NEVER returned in response
     * - Email uniqueness is case-insensitive
     * - Account is immediately active (no email verification in MVP)
     *
     * @param request RegisterRequest containing email, password, and optional fullName
     * @return AuthResponse with JWT token and user details (excluding password)
     *
     * @example Request Body:
     * {
     *   "email": "user@example.com",
     *   "password": "SecurePass123!",
     *   "fullName": "John Doe"
     * }
     *
     * @example Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "user": {
     *     "id": "uuid",
     *     "email": "user@example.com",
     *     "fullName": "John Doe",
     *     "role": "USER",
     *     "isActive": true
     *   }
     * }
     */
    @PostMapping("/register")
    @Operation(
        summary = "Register new user account",
        description = "Create a new user account with USER role. Returns JWT token for immediate authentication. Password is automatically hashed with BCrypt."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Registration successful - user created and JWT token issued",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Success",
                    summary = "Successful registration",
                    value = """
                    {
                      "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyLWlkIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE2MzI0MjAwMDAsImV4cCI6MTYzMjUwNjQwMH0...",
                      "user": {
                        "id": "1c3cc01f-0216-43d0-98b8-970df0daaa98",
                        "email": "user@example.com",
                        "fullName": "John Doe",
                        "role": "USER",
                        "isActive": true
                      }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - validation failed or email already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
                examples = {
                    @ExampleObject(
                        name = "Email Already Exists",
                        summary = "Duplicate email",
                        value = """
                        {
                          "type": "/errors/bad-request",
                          "title": "Bad Request",
                          "status": 400,
                          "detail": "User with email user@example.com already exists",
                          "instance": "/api/auth/register",
                          "timestamp": "2025-12-05T10:30:00Z"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Validation Error",
                        summary = "Invalid email format",
                        value = """
                        {
                          "type": "/errors/validation",
                          "title": "Validation Failed",
                          "status": 400,
                          "detail": "Email must be a valid email address",
                          "instance": "/api/auth/register",
                          "timestamp": "2025-12-05T10:30:00Z"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - unexpected system error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Registration details including email, password, and optional full name",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = RegisterRequest.class),
            examples = {
                @ExampleObject(
                    name = "Basic Registration",
                    summary = "Minimal registration",
                    value = """
                    {
                      "email": "newuser@parknexus.com",
                      "password": "SecurePassword123!"
                    }
                    """
                ),
                @ExampleObject(
                    name = "Full Registration",
                    summary = "Complete registration with full name",
                    value = """
                    {
                      "email": "john.doe@parknexus.com",
                      "password": "MySecurePass123!",
                      "fullName": "John Doe"
                    }
                    """
                )
            }
        )
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticate user and obtain JWT token.
     *
     * Validates user credentials and returns a JWT token for authenticated API access.
     *
     * Process Flow:
     * 1. Validate request (email and password present)
     * 2. Find user by email (case-insensitive)
     * 3. Verify password using BCrypt comparison
     * 4. Check if account is active (isActive = true)
     * 5. Generate JWT token with user claims
     * 6. Log login activity
     * 7. Return token and user details
     *
     * JWT Token Claims:
     * - sub: User ID (UUID)
     * - email: User's email address
     * - role: User's role (USER, ADMIN)
     * - iat: Issued at timestamp
     * - exp: Expiration timestamp (24 hours from issuance)
     *
     * Security Considerations:
     * - Passwords are compared using BCrypt (constant-time comparison)
     * - Failed login attempts should be rate-limited
     * - Account lockout after N failed attempts (recommended: implement)
     * - Inactive accounts (isActive = false) cannot login
     * - Login activity is logged for security audit
     *
     * Token Usage:
     * - Include token in subsequent requests: Authorization: Bearer {token}
     * - Token is valid for 24 hours
     * - Token cannot be revoked before expiration (stateless JWT)
     * - For token revocation, implement token blacklist (future enhancement)
     *
     * @param request LoginRequest containing email and password
     * @return AuthResponse with JWT token and user details
     *
     * @example Request Body:
     * {
     *   "email": "user@example.com",
     *   "password": "SecurePass123!"
     * }
     *
     * @example Response (200 OK):
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "user": {
     *     "id": "uuid",
     *     "email": "user@example.com",
     *     "fullName": "John Doe",
     *     "role": "USER",
     *     "isActive": true
     *   }
     * }
     */
    @PostMapping("/login")
    @Operation(
        summary = "Login and obtain JWT token",
        description = "Authenticate user credentials and receive a JWT token for API access. Token is valid for 24 hours."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login successful - JWT token issued",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = {
                    @ExampleObject(
                        name = "User Login",
                        summary = "Regular user login",
                        value = """
                        {
                          "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxYzNjYzAxZi0wMjE2LTQzZDAtOThiOC05NzBkZjBkYWFhOTgiLCJlbWFpbCI6ImpvaG4uZG9lQGV4YW1wbGUuY29tIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3MzM0MDAwMDAsImV4cCI6MTczMzQ4NjQwMH0...",
                          "user": {
                            "id": "1c3cc01f-0216-43d0-98b8-970df0daaa98",
                            "email": "john.doe@example.com",
                            "fullName": "John Doe",
                            "role": "USER",
                            "isActive": true
                          }
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Admin Login",
                        summary = "Admin user login",
                        value = """
                        {
                          "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbi1pZCIsImVtYWlsIjoiYWRtaW5AcGFya25leHVzLmNvbSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTczMzQwMDAwMCwiZXhwIjoxNzMzNDg2NDAwfQ...",
                          "user": {
                            "id": "admin-uuid",
                            "email": "admin@parknexus.com",
                            "fullName": "Admin User",
                            "role": "ADMIN",
                            "isActive": true
                          }
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - invalid credentials or inactive account",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
                examples = {
                    @ExampleObject(
                        name = "Invalid Credentials",
                        summary = "Wrong email or password",
                        value = """
                        {
                          "type": "/errors/unauthorized",
                          "title": "Unauthorized",
                          "status": 401,
                          "detail": "Invalid email or password",
                          "instance": "/api/auth/login",
                          "timestamp": "2025-12-05T10:30:00Z"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Inactive Account",
                        summary = "Account is deactivated",
                        value = """
                        {
                          "type": "/errors/unauthorized",
                          "title": "Unauthorized",
                          "status": 401,
                          "detail": "Account is inactive. Please contact support.",
                          "instance": "/api/auth/login",
                          "timestamp": "2025-12-05T10:30:00Z"
                        }
                        """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - validation failed",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
                examples = @ExampleObject(
                    name = "Missing Fields",
                    summary = "Email or password missing",
                    value = """
                    {
                      "type": "/errors/validation",
                      "title": "Validation Failed",
                      "status": 400,
                      "detail": "Email and password are required",
                      "instance": "/api/auth/login",
                      "timestamp": "2025-12-05T10:30:00Z"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests - rate limit exceeded (if rate limiting is enabled)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - unexpected system error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Login credentials (email and password)",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = LoginRequest.class),
            examples = {
                @ExampleObject(
                    name = "User Login",
                    summary = "Regular user credentials",
                    value = """
                    {
                      "email": "john.doe@example.com",
                      "password": "password123"
                    }
                    """
                ),
                @ExampleObject(
                    name = "Admin Login",
                    summary = "Admin credentials",
                    value = """
                    {
                      "email": "admin@parknexus.com",
                      "password": "password123"
                    }
                    """
                )
            }
        )
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}

