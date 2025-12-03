package org.codeup.parknexus.web.controller;

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
 * Public authentication endpoints: register and login.
 * - Register: creates USER with BCrypt-hashed password
 * - Login: returns JWT token with userId, email, role
 *
 * Keep payloads minimal; never return password hashes.
 *
 * author: TonyS-dev
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Registration returns token; email uniqueness enforced downstream
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // Login issues JWT; ensure inactive accounts are rejected in service
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
