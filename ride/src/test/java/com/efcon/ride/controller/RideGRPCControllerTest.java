package com.efcon.ride.controller;

import com.efcon.ride.dto.RideResponse;
import com.efcon.ride.grpc.stubs.GetRideByIdRequest;
import com.efcon.ride.grpc.stubs.GetRideByIdResponse;
import com.efcon.ride.grpc.stubs.Ride;
import com.efcon.ride.mapper.GRPCRideMapper;
import com.efcon.ride.service.RideService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class RideGRPCControllerTest {
    @Mock
    private RideService rideService;
    @Mock
    private GRPCRideMapper rideMapper;

    @InjectMocks
    private RideGRPCController rideGRPCController;

    @Test
    @SuppressWarnings("unchecked")
    void getRideByIdUseServiceAndBuildResponse() {
        var requestId = 1L;
        var request = Mockito.mock(GetRideByIdRequest.class);
        StreamObserver<GetRideByIdResponse> responseObserver = Mockito.mock(StreamObserver.class);

        Mockito.when(request.getId()).thenReturn(requestId);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(rideService.get(requestId)).thenReturn(rideResponse);

        var ride = Mockito.mock(Ride.class);
        Mockito.when(rideMapper.toGRPCRide(rideResponse)).thenReturn(ride);

        rideGRPCController.getRideById(request, responseObserver);

        Mockito.verify(rideService).get(requestId);
        Mockito.verify(responseObserver).onNext(any(GetRideByIdResponse.class));
        Mockito.verify(responseObserver).onCompleted();
    }
}