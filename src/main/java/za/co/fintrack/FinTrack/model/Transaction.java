package za.co.fintrack.FinTrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import za.co.fintrack.FinTrack.enums.TransactionStatus;
import za.co.fintrack.FinTrack.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity(name = "transactions")
public class Transaction {

    @Id
    private Long id;
    @Getter
    @Setter
    private Long from_account_id;
    @Getter
    @Setter
    private  Long to_account_id;
    @Getter
    @Setter
    private Long category_id;
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
    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;
}
