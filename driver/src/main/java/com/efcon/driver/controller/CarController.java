package com.efcon.driver.controller;

import com.efcon.driver.dto.CarRequest;
import com.efcon.driver.dto.CarResponse;
import com.efcon.driver.service.CarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService service;

    @GetMapping
    public List<CarResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public CarResponse getById(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<CarResponse> create(@RequestBody @Valid CarRequest car) {
        return ResponseEntity.status(201).body(service.create(car));
    }

    @PutMapping("/{id}")
    public CarResponse update(@PathVariable long id, @RequestBody @Valid CarRequest car) {
        return service.update(id, car);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
