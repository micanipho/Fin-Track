package za.co.fintrack.services.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.fintrack.enums.Role;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.UserDto;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.services.AuthenticationService;
import za.co.fintrack.services.UserService;
import za.co.fintrack.utils.PasswordValidator;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final Mapper<User, UserDto> mapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // Token blacklist - in production, use Redis or database
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpirationMs;

    @Override
    @Transactional
    public UserDetails authenticate(String username, String password) {
        // Find user by username or email
        Optional<User> userOpt = userService.findByUsernameOrEmail(username, username);

        if (userOpt.isEmpty()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userOpt.get();

        // Check if account is locked
        if (!user.isAccountNonLocked()) {
            userService.recordLoginAttempt(user.getId(), false);
            throw new LockedException("Account is locked due to multiple failed login attempts");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Record successful login
            userService.recordLoginAttempt(user.getId(), true);

            return userDetailsService.loadUserByUsername(user.getEmail());

        } catch (BadCredentialsException e) {
            // Record failed login attempt
            userService.recordLoginAttempt(user.getId(), false);
            throw e;
        }
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        return generateTokenInternal(userDetails, jwtExpirationMs);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        return generateTokenInternal(userDetails, refreshTokenExpirationMs);
    }

    private String generateTokenInternal(UserDetails userDetails, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities());

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public UserDetails validateToken(String token) {
        if (isTokenBlacklisted(token)) {
            throw new RuntimeException("Token has been invalidated");
        }

        try {
            String userName = extractUserName(token);
            return userDetailsService.loadUserByUsername(userName);
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    @Override
    public UserDetails refreshToken(String refreshToken) {
        if (isTokenBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token has been invalidated");
        }

        try {
            Claims claims = extractAllClaims(refreshToken);
            String username = claims.getSubject();

            // Validate that it's not expired
            if (claims.getExpiration().before(new Date())) {
                throw new RuntimeException("Refresh token has expired");
            }

            return userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token", e);
        }
    }

    @Override
    @Transactional
    public UserDto register(UserDto userDto) {
        // Validate input
        if (userService.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userService.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Validate password strength
        PasswordValidator.ValidationResult validation = PasswordValidator.validate(userDto.getPassword());
        if (!validation.isValid()) {
            throw new RuntimeException("Password validation failed: " + validation.getErrorMessage());
        }

        User userToSave = mapper.mapFrom(userDto);

        // Set default values
        if (userToSave.getRole() == null) {
            userToSave.setRole(Role.USER);
        }

        // Encode password
        userToSave.setPassword(passwordEncoder.encode(userToSave.getPassword()));

        // Set initial verification status
        userToSave.setEmailVerified(false);
        userToSave.setCreatedAt(LocalDateTime.now());
        userToSave.setUpdatedAt(LocalDateTime.now());

        User savedUser = userService.saveUser(userToSave);

        // Initiate email verification
        userService.initiateEmailVerification(savedUser.getId());

        return mapper.mapTo(savedUser);
    }

    @Override
    public void logout(String token) {
        tokenBlacklist.add(token);
        log.info("Token added to blacklist");
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    private String extractUserName(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
