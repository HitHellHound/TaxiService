package com.efcon.passenger.service;

import com.efcon.passenger.dto.PassengerRequestDTO;
import com.efcon.passenger.dto.PassengerResponseDTO;

import java.util.List;

public interface PassengerService {
    List<PassengerResponseDTO> getAll();
    PassengerResponseDTO get(Long id);
    PassengerResponseDTO create(PassengerRequestDTO passengerDTO);
    PassengerResponseDTO update(Long id, PassengerRequestDTO passengerDTO);
    void delete(Long id);
}
