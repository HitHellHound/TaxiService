package com.efcon.driver.controller;

import com.efcon.driver.dto.DriverRequest;
import com.efcon.driver.dto.DriverResponse;
import com.efcon.driver.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService service;

    @GetMapping
    public List<DriverResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public DriverResponse getById(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<DriverResponse> create(@RequestBody @Valid DriverRequest driver) {
        return ResponseEntity.status(201).body(service.create(driver));
    }

    @PutMapping("/{id}")
    public DriverResponse update(@PathVariable long id, @RequestBody @Valid DriverRequest driver) {
        return service.update(id, driver);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{driverId}/car/{carId}")
    public ResponseEntity<DriverResponse> attachCar(@PathVariable long driverId, @PathVariable long carId) {
        return ResponseEntity.status(201).body(service.attachCar(driverId, carId));
    }

    @DeleteMapping("/{driverId}/car")
    public ResponseEntity<Void> detachCarFromDriver(@PathVariable long driverId) {
        service.detachCar(driverId);
        return ResponseEntity.noContent().build();
    }
}
