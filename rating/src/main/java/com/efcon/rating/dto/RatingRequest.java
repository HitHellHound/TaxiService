package com.efcon.rating.dto;

import com.efcon.rating.validation.DependentField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@DependentField(field = "comment", dependsOn = "score")
public record RatingRequest(
        @Min(1) @Max(5) Integer score,
        String comment) {
}
