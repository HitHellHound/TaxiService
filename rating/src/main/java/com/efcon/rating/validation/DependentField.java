package com.efcon.rating.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DependentFields.class)
@Constraint(validatedBy = DependentFieldsValidator.class)
public @interface DependentField {
    String message() default "A dependent field has been provided, but the field it depends on is null";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String field();
    String dependsOn();
}
