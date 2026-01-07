package com.efcon.ride.controller;

import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.ExternalBadRequestException;
import com.efcon.ride.exception.ExternalServiceException;
import com.efcon.ride.exception.IllegalRideStatusTransition;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(exception = {
            IllegalRideStatusTransition.class,
            ExternalBadRequestException.class})
    public String illegalStatusTransition(Exception exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(exception = EntityNotFoundException.class)
    public String entityNotFound(EntityNotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(exception = ConstraintViolationException.class)
    public String entityValidationFailed(ConstraintViolationException exception) {
        StringBuilder message = new StringBuilder("Invalid data passed: \n");
        for (ConstraintViolation<?> violation: exception.getConstraintViolations()) {
            message.append(violation.getPropertyPath())
                    .append(" -- ")
                    .append(violation.getMessage())
                    .append('\n');
        }
        return message.toString();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(exception = MethodArgumentNotValidException.class)
    public String validationFailed(MethodArgumentNotValidException exception) {
        StringBuilder message = new StringBuilder("Invalid data passed: \n");
        for (ObjectError error: exception.getBindingResult().getAllErrors()) {
            if (error instanceof FieldError fieldError) {
                message.append(fieldError.getField())
                        .append(" -- ")
                        .append(fieldError.getDefaultMessage())
                        .append('\n');
            }
        }
        return message.toString();
    }

    @ExceptionHandler(exception = DataIntegrityViolationException.class)
    public ResponseEntity<String> dbValidationFailed(DataIntegrityViolationException exception) {
        StringBuilder message = new StringBuilder("Invalid data passed: ");
        if (exception.getRootCause() instanceof PSQLException psqlException) {
            if (psqlException.getServerErrorMessage() != null)
                message.append(psqlException.getServerErrorMessage().getDetail());
        } else {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.status(400).body(message.toString());
    }

    @ExceptionHandler(exception = {
            CallNotPermittedException.class,
            ExternalServiceException.class
    })
    public ResponseEntity<String> externalServiceDenied(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("Retry-After", "10")
                .body("One of the internal services is currently unavailable, please try again later");
    }
}
