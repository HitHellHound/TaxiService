package com.efcon.ride.validation;

import com.efcon.ride.model.Ride;
import com.efcon.ride.model.RideStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ActiveRideDriverNotNullValidator implements ConstraintValidator<ActiveRideDriverNotNull, Ride> {
    private final Set<RideStatus> activeStatuses = new HashSet<>();

    @Override
    public boolean isValid(Ride ride, ConstraintValidatorContext context) {
        if (ride == null || !activeStatuses.contains(ride.getStatus())) {
            return true;
        }

        if (ride.getDriverId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Driver must be assigned when ride is in status " + ride.getStatus())
                    .addPropertyNode("driverId")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    @Override
    public void initialize(ActiveRideDriverNotNull constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        activeStatuses.addAll(Arrays.stream(constraintAnnotation.activeStatuses()).toList());
    }
}
