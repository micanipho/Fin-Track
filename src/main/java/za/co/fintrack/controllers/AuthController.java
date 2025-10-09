package za.co.fintrack.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import za.co.fintrack.auth.AuthResponse;
import za.co.fintrack.auth.LoginRequest;
import za.co.fintrack.auth.SignUpRequest;
import za.co.fintrack.models.dtos.ForgotPasswordDto;
import za.co.fintrack.models.dtos.ResetPasswordDto;
import za.co.fintrack.models.dtos.UserDto;
import za.co.fintrack.services.AuthenticationService;
import za.co.fintrack.services.UserService;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            UserDetails userDetails = authenticationService.authenticate(
                    loginRequest.getUsername().trim(),
                    loginRequest.getPassword()
            );

            String accessToken = authenticationService.generateToken(userDetails);
            String refreshToken = authenticationService.generateRefreshToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(86400)
                    .build());

        } catch (LockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("error", "Account is locked due to multiple failed login attempts"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            // Validate password confirmation
            if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Passwords do not match"));
            }

            // Create UserDto from SignUpRequest
            UserDto userDto = UserDto.builder()
                    .username(signUpRequest.getUsername().trim())
                    .email(signUpRequest.getEmail().trim())
                    .password(signUpRequest.getPassword())
                    .role(za.co.fintrack.enums.Role.USER)
                    .active(true)
                    .build();

            // Register the user
            UserDto registeredUser = authenticationService.register(userDto);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Registration successful. Please check your email to verify your account.",
                            "userId", registeredUser.getId()
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Refresh token is required"));
            }

            UserDetails userDetails = authenticationService.refreshToken(refreshToken);
            String newAccessToken = authenticationService.generateToken(userDetails);
            String newRefreshToken = authenticationService.generateRefreshToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(86400)
                    .build());

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Token refresh failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                authenticationService.logout(token);
            }

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Logout failed"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {
            userService.initiatePasswordReset(forgotPasswordDto.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "If an account with that email exists, a password reset link has been sent."
            ));
        } catch (Exception e) {
            log.error("Password reset initiation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process password reset request"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            if (!resetPasswordDto.getNewPassword().equals(resetPasswordDto.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Passwords do not match"));
            }

            userService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Password reset failed"));
        }
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Email verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Email verification failed"));
        }
    }
}
