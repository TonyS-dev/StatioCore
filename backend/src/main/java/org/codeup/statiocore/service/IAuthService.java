package org.codeup.statiocore.service;

import org.codeup.statiocore.web.dto.auth.AuthResponse;
import org.codeup.statiocore.web.dto.auth.LoginRequest;
import org.codeup.statiocore.web.dto.auth.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}

