package za.co.fintrack.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.fintrack.models.dtos.ChangePasswordDto;
import za.co.fintrack.models.dtos.UserProfileDto;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.repositories.UserRepository;
import za.co.fintrack.services.EmailService;
import za.co.fintrack.services.UserService;
import za.co.fintrack.utils.PasswordValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.account-lockout-duration:30}")
    private int accountLockoutDurationMinutes;

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(Long userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if username is being changed and if it's available
        if (!user.getUsername().equals(profileDto.getUsername()) &&
            existsByUsername(profileDto.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Check if email is being changed and if it's available
        boolean emailChanged = !user.getEmail().equals(profileDto.getEmail());
        if (emailChanged && existsByEmail(profileDto.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        user.setUsername(profileDto.getUsername());
        user.setEmail(profileDto.getEmail());
        user.setFirstName(profileDto.getFirstName());
        user.setLastName(profileDto.getLastName());
        user.setPhoneNumber(profileDto.getPhoneNumber());
        user.setProfilePictureUrl(profileDto.getProfilePictureUrl());

        // If email changed, reset verification
        if (emailChanged) {
            user.setEmailVerified(false);
            initiateEmailVerification(userId);
        }

        User savedUser = userRepository.save(user);
        return getUserProfile(savedUser.getId());
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        // Check password strength
        PasswordValidator.ValidationResult validation = PasswordValidator.validate(changePasswordDto.getNewPassword());
        if (!validation.isValid()) {
            throw new RuntimeException("Password validation failed: " + validation.getErrorMessage());
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Don't reveal if email exists or not for security
            log.info("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetExpires(LocalDateTime.now().plusHours(1));

        userRepository.save(user);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getPasswordResetExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Validate new password
        PasswordValidator.ValidationResult validation = PasswordValidator.validate(newPassword);
        if (!validation.isValid()) {
            throw new RuntimeException("Password validation failed: " + validation.getErrorMessage());
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpires(null);
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void initiateEmailVerification(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpires(LocalDateTime.now().plusDays(1));

        userRepository.save(user);
        emailService.sendEmailVerification(user.getEmail(), verificationToken, user.getUsername());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.getEmailVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        userRepository.verifyEmail(user.getId());
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
    }

    @Override
    @Transactional
    public void recordLoginAttempt(Long userId, boolean successful) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (successful) {
            user.setFailedLoginAttempts(0);
            user.setLastLogin(LocalDateTime.now());
            user.setAccountLockedUntil(null);
        } else {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= maxLoginAttempts) {
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(accountLockoutDurationMinutes));
                log.warn("Account locked for user ID: {} after {} failed attempts", userId, attempts);
            }
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void lockAccount(Long userId) {
        userRepository.lockAccount(userId, LocalDateTime.now().plusMinutes(accountLockoutDurationMinutes));
    }

    @Override
    @Transactional
    public void unlockAccount(Long userId) {
        userRepository.lockAccount(userId, null);
        userRepository.updateFailedLoginAttempts(userId, 0);
    }

    @Override
    public boolean isAccountLocked(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return !user.isAccountNonLocked();
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();

        List<User> expiredEmailVerifications = userRepository.findExpiredEmailVerifications(now);
        for (User user : expiredEmailVerifications) {
            user.setEmailVerificationToken(null);
            user.setEmailVerificationExpires(null);
            userRepository.save(user);
        }

        List<User> expiredPasswordResets = userRepository.findExpiredPasswordResets(now);
        for (User user : expiredPasswordResets) {
            user.setPasswordResetToken(null);
            user.setPasswordResetExpires(null);
            userRepository.save(user);
        }

        log.info("Cleaned up {} expired email verifications and {} expired password resets",
                expiredEmailVerifications.size(), expiredPasswordResets.size());
    }
}
