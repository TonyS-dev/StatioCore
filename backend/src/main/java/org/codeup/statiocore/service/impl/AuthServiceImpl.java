package org.codeup.statiocore.service.impl;

import lombok.RequiredArgsConstructor;
import org.codeup.statiocore.domain.User;
import org.codeup.statiocore.domain.enums.Role;
import org.codeup.statiocore.exception.BadRequestException;
import org.codeup.statiocore.exception.UnauthorizedException;
import org.codeup.statiocore.repository.IUserRepository;
import org.codeup.statiocore.security.TokenProvider;
import org.codeup.statiocore.service.IActivityLogService;
import org.codeup.statiocore.service.IAuthService;
import org.codeup.statiocore.web.dto.auth.AuthResponse;
import org.codeup.statiocore.web.dto.auth.LoginRequest;
import org.codeup.statiocore.web.dto.auth.RegisterRequest;
import org.codeup.statiocore.web.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements IAuthService {
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final IActivityLogService activityLogService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User already exists with this email");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();

        user = userRepository.save(user);

        // Log user registration
        activityLogService.log(user, "USER_REGISTERED", "New user account created");

        // Generate JWT token
        String token = tokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toDTO(user))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Validate input
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        // Check if user is active
        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is disabled");
        }

        // Generate JWT token
        String token = tokenProvider.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(userMapper.toDTO(user))
                .build();
    }
}

