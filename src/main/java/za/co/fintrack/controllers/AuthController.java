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
import za.co.fintrack.services.AuthenticationService;

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
            log.info("Received valid login request for username: {}", loginRequest.getUsername());

            UserDetails userDetails = authenticationService.authenticate(
                    loginRequest.getUsername().trim(),
                    loginRequest.getPassword()
            );

            String tokenValue = authenticationService.generateToken(userDetails);

            log.info("Authentication successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(tokenValue)
                    .expiresIn(86400)
                    .build());

        } catch (BadCredentialsException e) {
            log.warn("Bad credentials for username: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for username: {}, reason: {}",
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));
        } catch (Exception e) {
            log.error("Unexpected error during authentication", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}
