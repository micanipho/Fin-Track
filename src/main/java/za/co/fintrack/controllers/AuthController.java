package za.co.fintrack.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import za.co.fintrack.auth.AuthResponse;
import za.co.fintrack.auth.LoginRequest;
import za.co.fintrack.auth.SignUpRequest;
import za.co.fintrack.services.AuthenticationService;
import za.co.fintrack.models.dtos.UserDto;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {

            UserDetails userDetails = authenticationService.authenticate(
                    loginRequest.getUsername().trim(),
                    loginRequest.getPassword()
            );

            String tokenValue = authenticationService.generateToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(tokenValue)
                    .expiresIn(86400)
                    .build());

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));
        } catch (Exception e) {
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

            // Authenticate the newly registered user
            UserDetails userDetails = authenticationService.authenticate(
                    registeredUser.getUsername(),
                    signUpRequest.getPassword()
            );

            // Generate token
            String tokenValue = authenticationService.generateToken(userDetails);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AuthResponse.builder()
                            .token(tokenValue)
                            .expiresIn(86400)
                            .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }
}
