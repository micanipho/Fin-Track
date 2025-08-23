package za.co.fintrack.FinTrack.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "catagories")
public class Catagory {
    @Id
    @Getter
    private Long id;
    private Long userId;
    @Getter
    @Setter
    private String code;
    @Getter
    @Setter
    private String name;
}
