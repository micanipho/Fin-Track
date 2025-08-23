package za.co.fintrack.FinTrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import za.co.fintrack.FinTrack.enums.BudgetStatus;

import java.time.LocalDate;

@Entity(name = "budgets")
public class Budget {

    @Id
    private Long id;
    @Getter
    private Long userId;
    @Getter
    @Setter
    private LocalDate date;
    @Getter
    @Setter
    private BudgetStatus status;
}
