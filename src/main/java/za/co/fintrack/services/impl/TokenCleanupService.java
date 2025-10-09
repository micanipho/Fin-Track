package za.co.fintrack.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import za.co.fintrack.services.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final UserService userService;

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled token cleanup");
        try {
            userService.cleanupExpiredTokens();
            log.info("Token cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during token cleanup", e);
        }
    }
}
