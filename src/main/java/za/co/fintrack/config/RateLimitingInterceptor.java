package za.co.fintrack.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, AttemptInfo> attemptCounts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long TIME_WINDOW = 15 * 60 * 1000; // 15 minutes

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();

        // Only apply rate limiting to authentication endpoints
        if (isAuthEndpoint(requestUri)) {
            if (isRateLimited(clientIp)) {
                response.setStatus(429); // HTTP 429 Too Many Requests
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
                response.setContentType("application/json");
                return false;
            }

            // Record the attempt
            recordAttempt(clientIp);
        }

        return true;
    }

    private boolean isAuthEndpoint(String uri) {
        return uri.startsWith("/api/auth/login") ||
               uri.startsWith("/api/auth/register") ||
               uri.startsWith("/api/auth/forgot-password") ||
               uri.startsWith("/api/auth/reset-password");
    }

    private boolean isRateLimited(String clientIp) {
        AttemptInfo attemptInfo = attemptCounts.get(clientIp);
        if (attemptInfo == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - attemptInfo.getFirstAttemptTime() > TIME_WINDOW) {
            // Reset the counter if time window has passed
            attemptCounts.remove(clientIp);
            return false;
        }

        return attemptInfo.getCount().get() >= MAX_ATTEMPTS;
    }

    private void recordAttempt(String clientIp) {
        long currentTime = System.currentTimeMillis();
        attemptCounts.compute(clientIp, (key, value) -> {
            if (value == null) {
                return new AttemptInfo(new AtomicInteger(1), currentTime);
            } else if (currentTime - value.getFirstAttemptTime() > TIME_WINDOW) {
                return new AttemptInfo(new AtomicInteger(1), currentTime);
            } else {
                value.getCount().incrementAndGet();
                return value;
            }
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private static class AttemptInfo {
        private final AtomicInteger count;
        private final long firstAttemptTime;

        public AttemptInfo(AtomicInteger count, long firstAttemptTime) {
            this.count = count;
            this.firstAttemptTime = firstAttemptTime;
        }

        public AtomicInteger getCount() {
            return count;
        }

        public long getFirstAttemptTime() {
            return firstAttemptTime;
        }
    }
}
