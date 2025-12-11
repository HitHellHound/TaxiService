package com.efcon.driver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CarRequest(@Pattern(
                            regexp = "^\\d{4}[A-Z]{2}-[1-8]$",
                            message = "must match Belarus format XXXXYY-Z"
                         ) String number,
                         @NotBlank String color,
                         @NotBlank String brand) {
}
