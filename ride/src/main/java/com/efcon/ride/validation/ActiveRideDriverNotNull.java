package com.efcon.ride.validation;

import com.efcon.ride.model.RideStatus;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ActiveRideDriverNotNullValidator.class)
public @interface ActiveRideDriverNotNull {
    String message() default "Active ride must have a driver assigned";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    RideStatus[] activeStatuses() default {
        RideStatus.ACCEPTED, RideStatus.DRIVING_TO_PASSENGER, RideStatus.DRIVING_TO_DESTINATION
    };
}
