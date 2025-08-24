package za.co.fintrack.valueObjects;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Money {

    private BigDecimal amount;
}
