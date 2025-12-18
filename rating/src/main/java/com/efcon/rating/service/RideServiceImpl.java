package com.efcon.rating.service;

import com.efcon.rating.dto.RideResponse;
import com.efcon.rating.mapper.RideMapper;
import com.efcon.ride.grpc.stubs.GetRideByIdRequest;
import com.efcon.ride.grpc.stubs.Ride;
import com.efcon.ride.grpc.stubs.RideGRPCServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {
    private final RideMapper rideMapper;

    @GrpcClient("ride-service")
    private RideGRPCServiceGrpc.RideGRPCServiceBlockingStub client;

    @Override
    public RideResponse get(Long id) {
        GetRideByIdRequest request = GetRideByIdRequest.newBuilder()
                .setId(id)
                .build();
        Ride ride = client.getRideById(request).getRide();
        return rideMapper.fromRPCDto(ride);
    }
}
