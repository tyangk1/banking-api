package com.banking.api.service;

public interface OtpService {

    /**
     * Generate a 6-digit OTP for a transfer, store in Redis with 5min TTL,
     * and send it to the user's email.
     * @return the OTP transaction token (UUID) to reference this OTP session
     */
    String generateAndSendOtp(String userId, String email, String transferDetails);

    /**
     * Verify the OTP code against the stored one.
     * @return true if valid and not expired
     */
    boolean verifyOtp(String otpToken, String otpCode);

    /**
     * Check if a transfer amount exceeds the OTP threshold.
     */
    boolean requiresOtp(java.math.BigDecimal amount);
}
