package com.efcon.ride.service;

import com.efcon.ride.dto.PassengerResponse;
import com.efcon.ride.dto.RideRequest;
import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.event.RideAcceptedEvent;
import com.efcon.ride.event.RideCanceledEvent;
import com.efcon.ride.event.RideCompletedEvent;
import com.efcon.ride.event.RideCreatedEvent;
import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.IllegalRideStatusTransitionException;
import com.efcon.ride.mapper.RideInfoMapper;
import com.efcon.ride.mapper.RideMapper;
import com.efcon.ride.model.DriverInfo;
import com.efcon.ride.model.DriverStatus;
import com.efcon.ride.model.Ride;
import com.efcon.ride.model.RideStatus;
import com.efcon.ride.repository.RideRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {
    private final ApplicationEventPublisher eventPublisher;
    private final PassengerService passengerService;
    private final DriverInfoService driverInfoService;
    private final RideRepository repository;
    private final RideMapper mapper;
    private final RideInfoMapper rideInfoMapper;

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
        Ride newRide = repository.save(mapper.fromRequest(request));
        eventPublisher.publishEvent(new RideCreatedEvent(newRide.getId(), rideInfoMapper.toRideInfo(newRide)));
        return mapper.toResponse(newRide);
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
    @Transactional
    public RideResponse accept(Long id, Long driverId) {
        DriverInfo driverInfo = driverInfoService.get(driverId);
        if (driverInfo.getCarId() == null) {
            throw new IllegalRideStatusTransitionException("Driver with id " + driverId + " doesn't have a car");
        } else if (driverInfo.getStatus() != DriverStatus.FREE) {
            throw new IllegalRideStatusTransitionException("Driver with id " + driverId + " already on trip");
        }

        Ride ride = updateRideStatus(id, RideStatus.ACCEPTED);
        ride.setDriverId(driverId);

        driverInfoService.changeDriverStatus(driverId, DriverStatus.ON_TRIP);
        ride = repository.save(ride);

        eventPublisher.publishEvent(new RideAcceptedEvent(id, driverId));
        return mapper.toResponse(ride);
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
    @Transactional
    public RideResponse complete(Long id) {
        Ride ride = updateRideStatus(id, RideStatus.COMPLETED);
        driverInfoService.changeDriverStatus(ride.getDriverId(), DriverStatus.FREE);
        ride = repository.save(ride);

        eventPublisher.publishEvent(new RideCompletedEvent(id));
        return mapper.toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse cancel(Long id) {
        Ride ride = updateRideStatus(id, RideStatus.CANCELED);
        if (ride.getDriverId() != null) {
            driverInfoService.changeDriverStatus(ride.getDriverId(), DriverStatus.FREE);
        }
        ride = repository.save(ride);

        eventPublisher.publishEvent(new RideCanceledEvent(id));
        return mapper.toResponse(ride);
    }

    private Ride updateRideStatus(Long id, RideStatus newStatus) {
        Ride ride = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + id + " not found"));
        if (!RideStatus.isTransitionAllowed(ride.getStatus(), newStatus)) {
            throw new IllegalRideStatusTransitionException("Status transition from " + ride.getStatus()
                    + " to " + newStatus + " not allowed");
        }
        ride.setStatus(newStatus);
        return ride;
    }
}
