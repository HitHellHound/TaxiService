package com.efcon.ride.controller;

import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.grpc.stubs.GetRideByIdRequest;
import com.efcon.ride.grpc.stubs.GetRideByIdResponse;
import com.efcon.ride.grpc.stubs.Ride;
import com.efcon.ride.grpc.stubs.RideGRPCServiceGrpc;
import com.efcon.ride.service.RideService;
import com.google.protobuf.Timestamp;
import com.google.type.Decimal;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.ZoneOffset;

@GrpcService
@RequiredArgsConstructor
public class RideGRPCController extends RideGRPCServiceGrpc.RideGRPCServiceImplBase {
    private final RideService rideService;

    @Override
    public void getRideById(GetRideByIdRequest request, StreamObserver<GetRideByIdResponse> responseObserver) {
        RideResponse rideResponse = rideService.get(request.getId());

        GetRideByIdResponse response = GetRideByIdResponse.newBuilder()
                .setRide(convertToProtoBuffRide(rideResponse))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private Ride convertToProtoBuffRide(RideResponse rideResponse) {
        return Ride.newBuilder()
                .setId(rideResponse.id())
                .setDriverId(rideResponse.driverId())
                .setPassengerId(rideResponse.passengerId())
                .setStartAddress(rideResponse.startAddress())
                .setDestinationAddress(rideResponse.destinationAddress())
                .setCreatedAt(Timestamp.newBuilder()
                        .setSeconds(rideResponse.createdAt().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(rideResponse.createdAt().getNano())
                        .build())
                .setPrice(Decimal.newBuilder()
                        .setValue(rideResponse.price().toPlainString())
                        .build())
                .build();
    }
}
