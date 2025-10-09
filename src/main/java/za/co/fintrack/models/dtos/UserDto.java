package za.co.fintrack.models.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.fintrack.enums.Role;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    // Profile information
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePictureUrl;

    private Role role;
    @Builder.Default
    private boolean active = true;

    // Email verification
    @Builder.Default
    private boolean emailVerified = false;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpires;

    // Password reset
    private String passwordResetToken;
    private LocalDateTime passwordResetExpires;

    // Account lockout
    @Builder.Default
    private int failedLoginAttempts = 0;
    private LocalDateTime accountLockedUntil;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;

    // Computed field
    @JsonIgnore
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return String.format("%s %s",
            firstName != null ? firstName : "",
            lastName != null ? lastName : "").trim();
    }

    @JsonIgnore
    public boolean isAccountNonLocked() {
        return accountLockedUntil == null || accountLockedUntil.isBefore(LocalDateTime.now());
    }
}
