package com.efcon.ride.controller;

import com.efcon.ride.service.CRUDService;
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
}
