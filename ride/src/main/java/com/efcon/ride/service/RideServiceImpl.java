package com.efcon.ride.service;

import com.efcon.ride.dto.RideRequestDTO;
import com.efcon.ride.dto.RideResponseDTO;
import com.efcon.ride.mapper.RideMapper;
import com.efcon.ride.model.Ride;
import com.efcon.ride.model.RideStatus;
import com.efcon.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RideServiceImpl extends AbstractCRUDService<Ride, RideRequestDTO, RideResponseDTO> implements RideService{
    private final RideRepository repository;
    private final RideMapper mapper;

    @Override
    public RideResponseDTO accept(Long id, Long driverId) {
        Ride ride = updateRideStatus(id, RideStatus.ACCEPTED);
        ride.setDriverId(driverId);
        return mapper.toResponseDto(repository.save(ride));
    }

    @Override
    public RideResponseDTO driveToPassenger(Long id) {
        return mapper.toResponseDto(repository.save(updateRideStatus(id, RideStatus.DRIVING_TO_PASSENGER)));
    }

    @Override
    public RideResponseDTO driveToDestination(Long id) {
        return mapper.toResponseDto(repository.save(updateRideStatus(id, RideStatus.DRIVING_TO_DESTINATION)));
    }

    @Override
    public RideResponseDTO complete(Long id) {
        return mapper.toResponseDto(repository.save(updateRideStatus(id, RideStatus.COMPLETED)));
    }

    @Override
    public RideResponseDTO cancel(Long id) {
        return mapper.toResponseDto(repository.save(updateRideStatus(id, RideStatus.CANCELED)));
    }

    private Ride updateRideStatus(Long id, RideStatus newStatus) {
        Ride ride = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(getEntityName() + " with id " + id + " not found"));
        if (!RideStatus.isTransitionAllowed(ride.getStatus(), newStatus)) {
            throw new IllegalStateException("Status transition from " + ride.getStatus()
                    + " to " + newStatus + " not allowed");
        }
        ride.setStatus(newStatus);
        return ride;
    }

    @Override
    protected RideRepository getRepository() {
        return repository;
    }

    @Override
    protected RideMapper getMapper() {
        return mapper;
    }

    @Override
    protected String getEntityName() {
        return "Ride";
    }
}
