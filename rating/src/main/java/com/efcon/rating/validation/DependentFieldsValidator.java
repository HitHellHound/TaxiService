package com.efcon.rating.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

public class DependentFieldsValidator implements ConstraintValidator<DependentField, Object> {
    private String field;
    private String dependsOn;

    @Override
    public void initialize(DependentField constraintAnnotation) {
        this.field = constraintAnnotation.field();
        this.dependsOn = constraintAnnotation.dependsOn();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        try {
            Object fieldValue = getFieldValue(object, field);
            Object dependsOnValue = getFieldValue(object, dependsOn);

            if (fieldValue != null && dependsOnValue == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Field '" + field + "' is provided, but field '"
                                + dependsOn + "' it depends on is null")
                        .addPropertyNode(dependsOn)
                        .addConstraintViolation();
                return false;
            }

            return true;
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalStateException(exception.toString());
        }
        catch (Exception exception) {
            return false;
        }
    }

    private Object getFieldValue(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field reflField = object.getClass().getDeclaredField(fieldName);
        reflField.setAccessible(true);
        return reflField.get(object);
    }
}
