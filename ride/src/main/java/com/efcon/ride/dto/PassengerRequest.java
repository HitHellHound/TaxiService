package com.efcon.ride.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PassengerRequest(@NotBlank String name,
                               @Email @NotBlank String email,
                               @Pattern(
                                       regexp = "^\\+375(25|29|33|44)\\d{7}$",
                                       message = "must match Belarus format +375XXYYYYYYY"
                               ) @NotBlank String phone) {
}
