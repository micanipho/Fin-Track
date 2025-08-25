package za.co.fintrack.domain;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Money {

    private BigDecimal amount;
}
