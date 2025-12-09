package com.efcon.passenger.controller;

import com.efcon.passenger.service.CRUDService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractCRUDController<K, T> {
    protected abstract CRUDService<K, T> getService();

    @GetMapping
    public List<T> getAll() {
        return getService().getAll();
    }

    @GetMapping("/{id}")
    public T getById(@PathVariable long id) {
        return getService().get(id);
    }

    @PostMapping
    public ResponseEntity<T> create(@RequestBody K carDTO) {
        return ResponseEntity.status(201).body(getService().create(carDTO));
    }

    @PutMapping("/{id}")
    public T update(@PathVariable long id, @RequestBody K carDTO) {
        return getService().update(id, carDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        getService().delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(exception = NoSuchElementException.class)
    public ResponseEntity<String> entityNotFound(NoSuchElementException exception) {
        return ResponseEntity.status(404).body(exception.getMessage());
    }

    @ExceptionHandler(exception = ConstraintViolationException.class)
    public ResponseEntity<String> passengerNotFound(ConstraintViolationException exception) {
        StringBuilder message = new StringBuilder("Invalid data passed: \n");
        for (ConstraintViolation<?> violation: exception.getConstraintViolations()) {
            message.append(violation.getPropertyPath())
                    .append(" -- ")
                    .append(violation.getMessage())
                    .append('\n');
        }
        return ResponseEntity.status(400).body(message.toString());
    }
}