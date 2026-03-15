package com.banking.api.service.impl;

import com.banking.api.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    private static final String OTP_PREFIX = "otp:";
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final BigDecimal OTP_THRESHOLD = new BigDecimal("5000000"); // 5 million VND
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String generateAndSendOtp(String userId, String email, String transferDetails) {
        // Generate 6-digit OTP
        String otpCode = String.format("%06d", random.nextInt(1000000));
        String otpToken = UUID.randomUUID().toString();

        // Store in Redis with TTL
        String redisKey = OTP_PREFIX + otpToken;
        redisTemplate.opsForValue().set(redisKey, otpCode, OTP_TTL);

        log.info("Generated OTP for user: {}, token: {}, code: {}", userId, otpToken, otpCode);

        // Send email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("🔐 Mã xác thực giao dịch (OTP) — Premium Banking");
            message.setText(
                "Xin chào,\n\n" +
                "Mã xác thực giao dịch (OTP) của bạn là:\n\n" +
                "    " + otpCode + "\n\n" +
                "Chi tiết giao dịch:\n" +
                transferDetails + "\n\n" +
                "⏰ Mã OTP có hiệu lực trong 5 phút.\n" +
                "⚠️ Không chia sẻ mã này với bất kỳ ai.\n\n" +
                "Trân trọng,\n" +
                "Premium Banking Team"
            );
            mailSender.send(message);
            log.info("OTP email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}. OTP still stored in Redis.", email, e);
            // Still return token — user can see OTP in logs during dev
        }

        return otpToken;
    }

    @Override
    public boolean verifyOtp(String otpToken, String otpCode) {
        String redisKey = OTP_PREFIX + otpToken;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            log.warn("OTP expired or invalid token: {}", otpToken);
            return false;
        }

        if (storedOtp.equals(otpCode)) {
            // Delete after successful verification (one-time use)
            redisTemplate.delete(redisKey);
            log.info("OTP verified successfully for token: {}", otpToken);
            return true;
        }

        log.warn("OTP mismatch for token: {}", otpToken);
        return false;
    }

    @Override
    public boolean requiresOtp(BigDecimal amount) {
        return amount.compareTo(OTP_THRESHOLD) >= 0;
    }
}
