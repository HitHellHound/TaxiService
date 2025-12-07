package com.efcon.ride.mapper;

import com.efcon.ride.dto.RideRequestDTO;
import com.efcon.ride.dto.RideResponseDTO;
import com.efcon.ride.model.Ride;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RideMapper extends CRUDMapper<Ride, RideRequestDTO, RideResponseDTO> {
    @Override
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "status", expression = "java(RideStatus.CREATED)")
    Ride fromRequestDto(RideRequestDTO dto);
}
