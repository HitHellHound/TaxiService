package com.efcon.passenger.service;

import com.efcon.passenger.dto.PassengerRequestDTO;
import com.efcon.passenger.dto.PassengerResponseDTO;
import com.efcon.passenger.mapper.PassengerMapper;
import com.efcon.passenger.model.Passenger;
import com.efcon.passenger.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl extends AbstractCRUDService<Passenger, PassengerRequestDTO, PassengerResponseDTO> implements PassengerService {
    private final PassengerRepository repository;
    private final PassengerMapper passengerMapper;

    @Override
    protected PassengerRepository getRepository() {
        return repository;
    }

    @Override
    protected PassengerMapper getMapper() {
        return passengerMapper;
    }

    @Override
    protected String getEntityName() {
        return "Passenger";
    }
}
