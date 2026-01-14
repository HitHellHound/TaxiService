package com.efcon.ride.mapper;

import com.efcon.ride.dto.RideInfo;
import com.efcon.ride.model.Ride;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RideInfoMapper {
    @Mapping(source = "id", target = "rideId")
    RideInfo toRideInfo(Ride ride);
}
