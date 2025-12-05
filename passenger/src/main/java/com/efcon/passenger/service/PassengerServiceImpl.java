package com.efcon.passenger.service;

import com.efcon.passenger.dto.PassengerRequestDTO;
import com.efcon.passenger.dto.PassengerResponseDTO;
import com.efcon.passenger.mapper.PassengerMapper;
import com.efcon.passenger.model.Passenger;
import com.efcon.passenger.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {
    private final PassengerRepository repository;
    private final PassengerMapper passengerMapper;

    @Override
    public List<PassengerResponseDTO> getAll() {
        return passengerMapper.toResponseDtoList(repository.findAll());
    }

    @Override
    public PassengerResponseDTO get(Long id) {
        Passenger passenger = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger with id " + id + " not found"));
        return passengerMapper.toResponseDto(passenger);
    }

    @Override
    public PassengerResponseDTO create(PassengerRequestDTO passengerDTO) {
        Passenger newPassenger = passengerMapper.fromRequestDto(passengerDTO);
        return passengerMapper.toResponseDto(repository.save(newPassenger));
    }

    @Override
    public PassengerResponseDTO update(Long id, PassengerRequestDTO passengerDTO) {
        Passenger passenger = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger with id " + id + " not found"));
        passengerMapper.updateEntityFromDto(passengerDTO, passenger);
        return passengerMapper.toResponseDto(repository.save(passenger));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
