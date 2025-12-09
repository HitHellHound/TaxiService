package com.efcon.rating.model;

import com.efcon.rating.validation.DependentField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rating")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DependentField(field = "passengerComment", dependsOn = "passengerScore")
@DependentField(field = "driverComment", dependsOn = "driverScore")
public class Rating {
    @Id
    private Long id;

    @Min(1)
    @Max(5)
    private Integer passengerScore;

    private String passengerComment;

    @Min(1)
    @Max(5)
    private Integer driverScore;

    private String driverComment;

    public Rating(Long id) {
        this.id = id;
    }
}
