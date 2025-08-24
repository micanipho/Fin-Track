package za.co.fintrack.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import za.co.fintrack.enums.GoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal target_amount;

    @Column(nullable = false)
    private BigDecimal current_amount;

    @Column(nullable = false)
    private LocalDate target_date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status;
}
