package com.efcon.ride.controller;

import com.efcon.ride.dto.DriverAssignmentDto;
import com.efcon.ride.dto.RideRequestDTO;
import com.efcon.ride.dto.RideResponseDTO;
import com.efcon.ride.service.CRUDService;
import com.efcon.ride.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
public class RideController extends AbstractCRUDController<RideRequestDTO, RideResponseDTO> {
    private final RideService service;

    @PostMapping("/{id}/accept")
    public RideResponseDTO acceptRide(@PathVariable long id, @RequestBody @Valid DriverAssignmentDto driver) {
        return service.accept(id, driver.driverId());
    }

    @PostMapping("/{id}/drive-to-passenger")
    public RideResponseDTO driveToPassenger(@PathVariable long id) {
        return service.driveToPassenger(id);
    }

    @PostMapping("/{id}/drive-to-destination")
    public RideResponseDTO driveToDestination(@PathVariable long id) {
        return service.driveToDestination(id);
    }

    @PostMapping("/{id}/complete")
    public RideResponseDTO completeRide(@PathVariable long id) {
        return service.complete(id);
    }

    @PostMapping("/{id}/cancel")
    public RideResponseDTO cancelRide(@PathVariable long id) {
        return service.cancel(id);
    }

    @ExceptionHandler(exception = IllegalStateException.class)
    public ResponseEntity<String> illegalStatusTransition(IllegalStateException exception) {
        return ResponseEntity.status(409).body(exception.getMessage());
    }

    @Override
    protected CRUDService<RideRequestDTO, RideResponseDTO> getService() {
        return service;
    }
}
