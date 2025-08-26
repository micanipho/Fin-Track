package za.co.fintrack.models.dtos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.fintrack.enums.AccountStatus;
import za.co.fintrack.enums.AccountType;
import za.co.fintrack.models.entities.User;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {

    private Long id;
    private User user;
    private String name;
    private AccountType type;
    private AccountStatus status;
    private BigDecimal balance;
}
