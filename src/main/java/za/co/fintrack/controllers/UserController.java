package za.co.fintrack.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import za.co.fintrack.models.dtos.ChangePasswordDto;
import za.co.fintrack.models.dtos.UserProfileDto;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.services.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            Long userId = getCurrentUserId();
            UserProfileDto profile = userService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Failed to get user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve profile"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileDto profileDto) {
        try {
            Long userId = getCurrentUserId();
            UserProfileDto updatedProfile = userService.updateUserProfile(userId, profileDto);
            return ResponseEntity.ok(updatedProfile);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update profile"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        try {
            Long userId = getCurrentUserId();
            userService.changePassword(userId, changePasswordDto);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to change password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to change password"));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendEmailVerification() {
        try {
            Long userId = getCurrentUserId();
            userService.initiateEmailVerification(userId);
            return ResponseEntity.ok(Map.of("message", "Verification email sent"));
        } catch (Exception e) {
            log.error("Failed to resend email verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send verification email"));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getId();
    }
}
