package com.efcon.passenger.mapper;

import com.efcon.passenger.dto.PassengerRequestDTO;
import com.efcon.passenger.dto.PassengerResponseDTO;
import com.efcon.passenger.model.Passenger;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PassengerMapper {
    PassengerResponseDTO toResponseDto(Passenger passenger);
    List<PassengerResponseDTO> toResponseDtoList(List<Passenger> passengers);
    Passenger fromRequestDto(PassengerRequestDTO passengerDTO);
    void updateEntityFromDto(PassengerRequestDTO passengerDTO, @MappingTarget Passenger passenger);
}
