package za.co.fintrack.FinTrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import za.co.fintrack.FinTrack.enums.AccountStatus;
import za.co.fintrack.FinTrack.enums.AccountType;
import za.co.fintrack.FinTrack.valueObjects.Money;

import java.math.BigDecimal;

@Entity(name = "accounts")
public class Account {

    @Id
    private Long id;
    @Getter
    @Setter
    private Long user_id;
    @Getter
    @Setter
    private  String name;
    @Getter
    @Setter
    private AccountType type;
    @Getter
    @Setter
    private AccountStatus status;
    @Getter
    @Setter
    private BigDecimal balance;
}
