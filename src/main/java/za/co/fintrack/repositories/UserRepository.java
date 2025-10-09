package za.co.fintrack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.fintrack.models.entities.User;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByEmailVerificationToken(String token);
    Optional<User> findByPasswordResetToken(String token);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = :attempts WHERE u.id = :userId")
    void updateFailedLoginAttempts(@Param("userId") Long userId, @Param("attempts") int attempts);

    @Modifying
    @Query("UPDATE User u SET u.accountLockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true, u.emailVerificationToken = null, u.emailVerificationExpires = null WHERE u.id = :userId")
    void verifyEmail(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE u.emailVerificationExpires < :now AND u.emailVerified = false")
    java.util.List<User> findExpiredEmailVerifications(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.passwordResetExpires < :now")
    java.util.List<User> findExpiredPasswordResets(@Param("now") LocalDateTime now);
}
