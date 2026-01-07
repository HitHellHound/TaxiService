package com.efcon.rating.external.client;

import com.efcon.ride.grpc.stubs.Ride;

public interface RideGrpcClient {
    Ride getRideById(Long id);
}
