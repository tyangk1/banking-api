package com.banking.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter using Redis sliding window counter.
 * 
 * Limits:
 *   - API requests: 100/minute per IP
 *   - Auth endpoints: 10/minute per IP (brute-force protection)
 *   - Transfer endpoints: 20/minute per IP
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final int DEFAULT_LIMIT = 100;
    private static final int AUTH_LIMIT = 10;
    private static final int TRANSFER_LIMIT = 20;
    private static final long WINDOW_SECONDS = 60;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        String path = request.getRequestURI();
        int limit = resolveLimit(path);

        String key = "rate_limit:" + clientIp + ":" + resolveBucket(path);

        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount != null && currentCount == 1) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        // Set rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, limit - (currentCount != null ? currentCount : 0))));
        response.setHeader("X-RateLimit-Reset", String.valueOf(WINDOW_SECONDS));

        if (currentCount != null && currentCount > limit) {
            log.warn("⚠️ Rate limit exceeded: ip={}, path={}, count={}/{}", clientIp, path, currentCount, limit);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"success":false,"message":"Too many requests. Please try again later.","data":null}
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private int resolveLimit(String path) {
        if (path.contains("/auth/")) return AUTH_LIMIT;
        if (path.contains("/transactions/transfer")) return TRANSFER_LIMIT;
        return DEFAULT_LIMIT;
    }

    private String resolveBucket(String path) {
        if (path.contains("/auth/")) return "auth";
        if (path.contains("/transactions/")) return "transactions";
        return "api";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
