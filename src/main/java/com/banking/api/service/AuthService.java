package com.banking.api.service;

import com.banking.api.model.dto.request.LoginRequest;
import com.banking.api.model.dto.request.RegisterRequest;
import com.banking.api.model.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);
}
