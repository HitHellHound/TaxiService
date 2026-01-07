package com.efcon.ride.service;

import com.efcon.ride.dto.DriverResponse;
import com.efcon.ride.dto.PassengerResponse;
import com.efcon.ride.dto.RideRequest;
import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.IllegalRideStatusTransition;
import com.efcon.ride.mapper.RideMapper;
import com.efcon.ride.model.Ride;
import com.efcon.ride.model.RideStatus;
import com.efcon.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {
    private final PassengerService passengerService;
    private final DriverService driverService;
    private final RideRepository repository;
    private final RideMapper mapper;

    @Override
    public List<RideResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    public RideResponse get(Long id) {
        Ride ride = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + id + " not found"));
        return mapper.toResponse(ride);
    }

    @Override
    public RideResponse create(RideRequest request) {
        PassengerResponse passenger = passengerService.get(request.passengerId());
        Ride newRide = mapper.fromRequest(request);
        return mapper.toResponse(repository.save(newRide));
    }

    @Override
    public RideResponse update(Long id, RideRequest request) {
        Ride ride = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + id + " not found"));
        mapper.updateEntityFromRequest(request, ride);
        return mapper.toResponse(repository.save(ride));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public RideResponse accept(Long id, Long driverId) {
        DriverResponse driver = driverService.get(driverId);
        if (driver.car() == null) {
            throw new IllegalRideStatusTransition("Driver with id " + driverId + " doesn't have a car");
        }
        Ride ride = updateRideStatus(id, RideStatus.ACCEPTED);
        ride.setDriverId(driverId);
        return mapper.toResponse(repository.save(ride));
    }

    @Override
    public RideResponse driveToPassenger(Long id) {
        return mapper.toResponse(repository.save(updateRideStatus(id, RideStatus.DRIVING_TO_PASSENGER)));
    }

    @Override
    public RideResponse driveToDestination(Long id) {
        return mapper.toResponse(repository.save(updateRideStatus(id, RideStatus.DRIVING_TO_DESTINATION)));
    }

    @Override
    public RideResponse complete(Long id) {
        return mapper.toResponse(repository.save(updateRideStatus(id, RideStatus.COMPLETED)));
    }

    @Override
    public RideResponse cancel(Long id) {
        return mapper.toResponse(repository.save(updateRideStatus(id, RideStatus.CANCELED)));
    }

    private Ride updateRideStatus(Long id, RideStatus newStatus) {
        Ride ride = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + id + " not found"));
        if (!RideStatus.isTransitionAllowed(ride.getStatus(), newStatus)) {
            throw new IllegalRideStatusTransition("Status transition from " + ride.getStatus()
                    + " to " + newStatus + " not allowed");
        }
        ride.setStatus(newStatus);
        return ride;
    }
}
