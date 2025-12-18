package com.efcon.rating.mapper;

import com.efcon.rating.dto.RideResponse;
import com.efcon.rating.dto.RideStatus;
import com.efcon.ride.grpc.stubs.Ride;
import com.google.protobuf.Timestamp;
import com.google.type.Decimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface RideMapper {
    @Mapping(target = "status", source = "status", qualifiedByName = "toRideStatus")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toLocalDateTime")
    @Mapping(target = "price", source = "price", qualifiedByName = "toBigDecimal")
    RideResponse fromRPCDto(Ride ride);

    @Named("toRideStatus")
    default RideStatus toRideStatus(com.efcon.ride.grpc.stubs.RideStatus grpcStatus) {
        return RideStatus.valueOf(grpcStatus.name());
    }

    @Named("toLocalDateTime")
    default LocalDateTime toLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos(), ZoneOffset.UTC);
    }

    @Named("toBigDecimal")
    default BigDecimal toBigDecimal(Decimal decimal) {
        if (decimal == null) {
            return null;
        }
        return new BigDecimal(decimal.getValue());
    }
}
