package com.efcon.ride.mapper;

import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.grpc.stubs.Ride;
import com.efcon.ride.grpc.stubs.RideStatus;
import com.google.protobuf.Timestamp;
import com.google.type.Decimal;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GRPCRideMapper {
    @Mapping(target = "status", source = "status", qualifiedByName = "toGRPCRideStatus")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toTimestamp")
    @Mapping(target = "price", source = "price", qualifiedByName = "toDecimal")
    Ride toGRPCRide(RideResponse response);

    @Named("toGRPCRideStatus")
    default RideStatus toGRPCRideStatus(com.efcon.ride.model.RideStatus status) {
        return RideStatus.valueOf(status.name());
    }

    @Named("toTimestamp")
    default Timestamp toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Timestamp.newBuilder()
                .setSeconds(localDateTime.toEpochSecond(ZoneOffset.UTC))
                .setNanos(localDateTime.getNano())
                .build();
    }

    @Named("toDecimal")
    default Decimal toDecimal(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }
        return Decimal.newBuilder()
                .setValue(bigDecimal.toPlainString())
                .build();
    }
}
