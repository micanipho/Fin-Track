package za.co.fintrack.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.fintrack.enums.AccountStatus;
import za.co.fintrack.enums.AccountType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private Long id;
    private Long userId; // Changed from User entity to just user ID
    private String name;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal balance;

    // Optional: Include user details for response
    private UserDto user;
}
