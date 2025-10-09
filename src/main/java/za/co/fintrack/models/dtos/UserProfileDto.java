package za.co.fintrack.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    private Long id;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Email should be valid")
    private String email;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String profilePictureUrl;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return username;
        }
        return String.format("%s %s",
            firstName != null ? firstName : "",
            lastName != null ? lastName : "").trim();
    }
}
