package za.co.fintrack.FinTrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import za.co.fintrack.FinTrack.enums.TransactionStatus;
import za.co.fintrack.FinTrack.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity(name = "transactions")
public class Transaction {

    @Id
    private Long id;
    @Getter
    @Setter
    private Long fromAccountId;
    @Getter
    @Setter
    private  Long toAccountId;
    @Getter
    @Setter
    private Long catagoryId;
    @Getter
    @Setter
    private BigDecimal amount;
    @Getter
    @Setter
    private TransactionType type;
    @Getter
    @Setter
    private TransactionStatus status;
    @Getter
    @Setter
    private LocalTime occuredAt;
}
