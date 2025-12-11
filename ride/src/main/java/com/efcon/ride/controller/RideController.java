package com.efcon.ride.controller;

import com.efcon.ride.dto.DriverAssignment;
import com.efcon.ride.dto.RideRequest;
import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@RequiredArgsConstructor
public class RideController {
    private final RideService service;

    @GetMapping
    public List<RideResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public RideResponse getById(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<RideResponse> create(@RequestBody @Valid RideRequest rideRequest) {
        return ResponseEntity.status(201).body(service.create(rideRequest));
    }

    @PutMapping("/{id}")
    public RideResponse update(@PathVariable long id, @RequestBody @Valid RideRequest rideRequest) {
        return service.update(id, rideRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/accept")
    public RideResponse acceptRide(@PathVariable long id, @RequestBody @Valid DriverAssignment driver) {
        return service.accept(id, driver.driverId());
    }

    @PostMapping("/{id}/drive-to-passenger")
    public RideResponse driveToPassenger(@PathVariable long id) {
        return service.driveToPassenger(id);
    }

    @PostMapping("/{id}/drive-to-destination")
    public RideResponse driveToDestination(@PathVariable long id) {
        return service.driveToDestination(id);
    }

    @PostMapping("/{id}/complete")
    public RideResponse completeRide(@PathVariable long id) {
        return service.complete(id);
    }

    @PostMapping("/{id}/cancel")
    public RideResponse cancelRide(@PathVariable long id) {
        return service.cancel(id);
    }
}
