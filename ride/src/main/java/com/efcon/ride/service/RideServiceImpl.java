package com.efcon.ride.service;

import com.efcon.ride.dto.RideRequestDTO;
import com.efcon.ride.dto.RideResponseDTO;
import com.efcon.ride.mapper.RideMapper;
import com.efcon.ride.model.Ride;
import com.efcon.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideServiceImpl extends AbstractCRUDService<Ride, RideRequestDTO, RideResponseDTO> implements RideService{
    private final RideRepository repository;
    private final RideMapper mapper;

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
