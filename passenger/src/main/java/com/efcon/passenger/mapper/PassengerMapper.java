package com.efcon.passenger.mapper;

import com.efcon.passenger.dto.PassengerRequest;
import com.efcon.passenger.dto.PassengerResponse;
import com.efcon.passenger.model.Passenger;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PassengerMapper extends CRUDMapper<Passenger, PassengerRequest, PassengerResponse>{
}
