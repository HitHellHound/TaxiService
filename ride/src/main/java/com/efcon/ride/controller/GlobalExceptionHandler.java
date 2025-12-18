package com.efcon.ride.controller;

import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.ExternalBadRequestException;
import com.efcon.ride.exception.IllegalRideStatusTransition;
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
    @ExceptionHandler(exception = IllegalRideStatusTransition.class)
    public String illegalStatusTransition(IllegalRideStatusTransition exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(exception = EntityNotFoundException.class)
    public String entityNotFound(EntityNotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(exception = ExternalBadRequestException.class)
    public String externalBadRequest(ExternalBadRequestException exception) {
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
}
