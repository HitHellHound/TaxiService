package com.efcon.ride.controller;

import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.grpc.stubs.GetRideByIdRequest;
import com.efcon.ride.grpc.stubs.GetRideByIdResponse;
import com.efcon.ride.grpc.stubs.RideGRPCServiceGrpc;
import com.efcon.ride.mapper.GRPCRideMapper;
import com.efcon.ride.service.RideService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class RideGRPCController extends RideGRPCServiceGrpc.RideGRPCServiceImplBase {
    private final RideService rideService;
    private final GRPCRideMapper rideMapper;

    @Override
    public void getRideById(GetRideByIdRequest request, StreamObserver<GetRideByIdResponse> responseObserver) {
        RideResponse rideResponse = rideService.get(request.getId());

        GetRideByIdResponse response = GetRideByIdResponse.newBuilder()
                .setRide(rideMapper.toGRPCRide(rideResponse))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
