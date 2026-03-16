package com.banking.api.controller;

import com.banking.api.model.dto.request.LoginRequest;
import com.banking.api.model.dto.request.RegisterRequest;
import com.banking.api.model.dto.response.ApiResponse;
import com.banking.api.model.dto.response.AuthResponse;
import com.banking.api.model.entity.LoginHistory;
import com.banking.api.model.entity.User;
import com.banking.api.model.enums.NotificationType;
import com.banking.api.repository.LoginHistoryRepository;
import com.banking.api.repository.UserRepository;
import com.banking.api.security.CustomUserPrincipal;
import com.banking.api.service.AuthService;
import com.banking.api.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user registration, login, and token management")
public class AuthController {

    private final AuthService authService;
    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account and return JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        AuthResponse response = authService.login(request);

        // Record login history
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user != null) {
                String ip = httpRequest.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty())
                    ip = httpRequest.getRemoteAddr();
                String ua = httpRequest.getHeader("User-Agent");

                LoginHistory history = LoginHistory.builder()
                        .user(user)
                        .ipAddress(ip != null ? ip : "unknown")
                        .userAgent(ua)
                        .device(extractDevice(ua))
                        .success(true)
                        .build();
                loginHistoryRepository.save(history);

                // Send login notification
                notificationService.persistAndSend(user.getId(), NotificationType.LOGIN,
                        "Đăng nhập mới",
                        "Đăng nhập từ " + (ip != null ? ip : "unknown") + " — " + extractDevice(ua),
                        null);
            }
        } catch (Exception e) {
            // Don't fail login if history logging fails
        }

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @GetMapping("/login-history")
    @Operation(summary = "Get login history", description = "Get paginated login history for current user")
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getLoginHistory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Map<String, Object>> result = loginHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(principal.getId(), PageRequest.of(page, size))
                .map(h -> Map.<String, Object>of(
                        "id", h.getId(),
                        "ipAddress", h.getIpAddress(),
                        "device", h.getDevice() != null ? h.getDevice() : "Unknown",
                        "userAgent", h.getUserAgent() != null ? h.getUserAgent() : "",
                        "success", h.isSuccess(),
                        "createdAt", h.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private String extractDevice(String ua) {
        if (ua == null)
            return "Unknown";
        if (ua.contains("Chrome"))
            return "Chrome Browser";
        if (ua.contains("Firefox"))
            return "Firefox Browser";
        if (ua.contains("Safari"))
            return "Safari Browser";
        if (ua.contains("Edge"))
            return "Edge Browser";
        return "Web Browser";
    }
}
