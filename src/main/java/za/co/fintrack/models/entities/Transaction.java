package za.co.fintrack.models.entities;

import jakarta.persistence.*;
import lombok.*;
import za.co.fintrack.enums.TransactionStatus;
import za.co.fintrack.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account from_account_id;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private  Account to_account_id;


    @OneToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category_id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;
}
