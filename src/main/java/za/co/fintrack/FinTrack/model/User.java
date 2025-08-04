package za.co.fintrack.FinTrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "users")
public record User() {
    @Id
    private static Long id;
    @Getter
    @Setter
    private static String username;
    @Getter
    @Setter
    private static String email;
    @Getter
    @Setter
    private static String password;
    @Getter
    @Setter
    private static String role;
}
