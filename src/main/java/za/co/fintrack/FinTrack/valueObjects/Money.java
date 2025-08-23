package za.co.fintrack.FinTrack.valueObjects;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class Money {

    @Getter
    @Setter
    private BigDecimal amount;
}
