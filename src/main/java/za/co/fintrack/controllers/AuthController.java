package za.co.fintrack.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Operation(
        summary = "User Login",
        description = "Authenticate user with username/email and password to receive JWT tokens",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login credentials",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    name = "Login Example",
                    value = "{\n  \"username\": \"john.doe@example.com\",\n  \"password\": \"SecurePassword123!\"\n}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Successful Login",
                    value = "{\n  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"expiresIn\": 86400\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Credentials",
                    value = "{\n  \"error\": \"Invalid username or password\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "423",
            description = "Account locked due to too many failed attempts",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Account Locked",
                    value = "{\n  \"error\": \"Account is locked due to multiple failed login attempts\"\n}"
                )
            )
        )
    })
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

    @Operation(
        summary = "User Registration",
        description = "Register a new user account with email verification",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration details",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SignUpRequest.class),
                examples = @ExampleObject(
                    name = "Registration Example",
                    value = "{\n  \"username\": \"johndoe\",\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"SecurePassword123!\",\n  \"confirmPassword\": \"SecurePassword123!\"\n}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Registration Success",
                    value = "{\n  \"message\": \"Registration successful. Please check your email to verify your account.\",\n  \"userId\": 1\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid registration data or passwords don't match",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = "{\n  \"error\": \"Passwords do not match\"\n}"
                )
            )
        )
    })
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

    @Operation(
        summary = "Refresh JWT Token",
        description = "Generate a new access token using a valid refresh token",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Refresh token request",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Refresh Token",
                    value = "{\n  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"\n}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Token",
                    value = "{\n  \"error\": \"Invalid or expired refresh token\"\n}"
                )
            )
        )
    })
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

    @Operation(
        summary = "User Logout",
        description = "Logout user and invalidate the current JWT token",
        security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success",
                    value = "{\n  \"message\": \"Logged out successfully\"\n}"
                )
            )
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
        @Parameter(description = "JWT token in Authorization header", required = true)
        @RequestHeader("Authorization") String authHeader) {
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

    @Operation(
        summary = "Forgot Password",
        description = "Initiate password reset process by sending reset email",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email address for password reset",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ForgotPasswordDto.class),
                examples = @ExampleObject(
                    name = "Forgot Password",
                    value = "{\n  \"email\": \"john.doe@example.com\"\n}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset email process initiated",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success",
                    value = "{\n  \"message\": \"If an account with that email exists, a password reset link has been sent.\"\n}"
                )
            )
        )
    })
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

    @Operation(
        summary = "Reset Password",
        description = "Reset user password using a valid reset token",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "New password and reset token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ResetPasswordDto.class),
                examples = @ExampleObject(
                    name = "Reset Password",
                    value = "{\n  \"token\": \"reset-token-here\",\n  \"newPassword\": \"NewSecurePassword123!\",\n  \"confirmPassword\": \"NewSecurePassword123!\"\n}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password reset successful",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success",
                    value = "{\n  \"message\": \"Password reset successfully\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid reset token or passwords don't match",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Token",
                    value = "{\n  \"error\": \"Invalid or expired reset token\"\n}"
                )
            )
        )
    })
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

    @Operation(
        summary = "Verify Email",
        description = "Verify user email address using email verification token",
        parameters = @Parameter(
            name = "token",
            description = "Email verification token",
            required = true,
            example = "verification-token-here"
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Success",
                    value = "{\n  \"message\": \"Email verified successfully\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired verification token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Token",
                    value = "{\n  \"error\": \"Invalid or expired verification token\"\n}"
                )
            )
        )
    })
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
