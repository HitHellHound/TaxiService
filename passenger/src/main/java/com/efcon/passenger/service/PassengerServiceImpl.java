package com.efcon.passenger.service;

import com.efcon.passenger.dto.PassengerRequest;
import com.efcon.passenger.dto.PassengerResponse;
import com.efcon.passenger.exception.EntityNotFoundException;
import com.efcon.passenger.mapper.PassengerMapper;
import com.efcon.passenger.model.Passenger;
import com.efcon.passenger.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {
    private final PassengerRepository repository;
    private final PassengerMapper passengerMapper;

    @Override
    public List<PassengerResponse> getAll() {
        return passengerMapper.toResponseList(repository.findAll());
    }

    @Override
    public PassengerResponse get(Long id) {
        Passenger entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Passenger with id " + id + " not found"));
        return passengerMapper.toResponse(entity);
    }

    @Override
    public PassengerResponse create(PassengerRequest request) {
        Passenger newPassenger = passengerMapper.fromRequest(request);
        return passengerMapper.toResponse(repository.save(newPassenger));
    }

    @Override
    public PassengerResponse update(Long id, PassengerRequest request) {
        Passenger entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Passenger with id " + id + " not found"));
        passengerMapper.updateEntityFromRequest(request, entity);
        return passengerMapper.toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
