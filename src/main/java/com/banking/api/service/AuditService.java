package com.banking.api.service;

import com.banking.api.model.entity.AuditLog;
import com.banking.api.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Audit service — logs security-sensitive actions asynchronously.
 * Uses @Async to avoid blocking the main request thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logAction(String userId, String action, String details, String status) {
        String ipAddress = "unknown";
        String userAgent = "unknown";

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                ipAddress = getClientIp(request);
                userAgent = request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not extract request info for audit log");
        }

        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent != null ? userAgent.substring(0, Math.min(255, userAgent.length())) : "unknown")
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("📝 [AUDIT] user={}, action={}, status={}, ip={}", userId, action, status, ipAddress);
    }

    // Convenience methods
    public void logLogin(String userId, boolean success) {
        logAction(userId, "LOGIN", success ? "Successful login" : "Failed login attempt",
                success ? "SUCCESS" : "FAILED");
    }

    public void logTransfer(String userId, String refNumber, String amount) {
        logAction(userId, "TRANSFER", "Transfer " + amount + " ref: " + refNumber, "SUCCESS");
    }

    public void logDeposit(String userId, String refNumber, String amount) {
        logAction(userId, "DEPOSIT", "Deposit " + amount + " ref: " + refNumber, "SUCCESS");
    }

    public void logAccountCreated(String userId, String accountNumber) {
        logAction(userId, "ACCOUNT_CREATED", "Account " + accountNumber + " created", "SUCCESS");
    }

    public void logAccountClosed(String userId, String accountNumber) {
        logAction(userId, "ACCOUNT_CLOSED", "Account " + accountNumber + " closed", "SUCCESS");
    }

    /**
     * Check if user has too many failed login attempts (account lockout protection).
     */
    public boolean isAccountLocked(String userId) {
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long failedAttempts = auditLogRepository.countByUserIdAndActionAndTimestampAfter(
                userId, "LOGIN", fiveMinutesAgo);
        return failedAttempts >= 5; // Lock after 5 failed attempts in 5 minutes
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
