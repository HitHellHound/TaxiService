package com.efcon.driver.controller;

import com.efcon.driver.dto.DriverRequestDTO;
import com.efcon.driver.dto.DriverResponseDTO;
import com.efcon.driver.service.CRUDService;
import com.efcon.driver.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
public class DriverController extends AbstractCRUDController<DriverRequestDTO, DriverResponseDTO> {
    private final DriverService service;

    @PatchMapping("/{driverId}/car/{carId}")
    public ResponseEntity<DriverResponseDTO> attachCar(@PathVariable long driverId, @PathVariable long carId) {
        return ResponseEntity.status(201).body(service.attachCar(driverId, carId));
    }

    @DeleteMapping("/{driverId}/car")
    public ResponseEntity<Void> detachCarFromDriver(@PathVariable long driverId) {
        service.detachCar(driverId);
        return ResponseEntity.noContent().build();
    }

    @Override
    protected CRUDService<DriverRequestDTO, DriverResponseDTO> getService() {
        return service;
    }
}
