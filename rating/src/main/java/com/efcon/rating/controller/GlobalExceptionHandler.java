package com.efcon.rating.controller;

import com.efcon.rating.exception.DocumentNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(exception = DocumentNotFoundException.class)
    public String entityNotFound(DocumentNotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(exception = ConstraintViolationException.class)
    public String validationFailed(ConstraintViolationException exception) {
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
}
