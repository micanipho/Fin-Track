package za.co.fintrack.services;

import za.co.fintrack.models.dtos.ChangePasswordDto;
import za.co.fintrack.models.dtos.UserProfileDto;
import za.co.fintrack.models.entities.User;

import java.util.Optional;


public interface UserService {

    User saveUser(User user);

    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Profile management
    UserProfileDto getUserProfile(Long userId);
    UserProfileDto updateUserProfile(Long userId, UserProfileDto profileDto);

    // Password management
    void changePassword(Long userId, ChangePasswordDto changePasswordDto);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);

    // Email verification
    void initiateEmailVerification(Long userId);
    void verifyEmail(String token);

    // Account security
    void recordLoginAttempt(Long userId, boolean successful);
    void lockAccount(Long userId);
    void unlockAccount(Long userId);
    boolean isAccountLocked(Long userId);

    // Cleanup operations
    void cleanupExpiredTokens();
}
