package za.co.fintrack.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.fintrack.enums.BudgetStatus;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction_audits")
public class TransactionAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetStatus status;
}