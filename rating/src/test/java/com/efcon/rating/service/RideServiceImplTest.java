package com.efcon.rating.service;

import com.efcon.rating.dto.RideResponse;
import com.efcon.rating.exception.EntityNotFoundException;
import com.efcon.rating.external.client.RideGrpcClient;
import com.efcon.rating.mapper.RideMapper;
import com.efcon.ride.grpc.stubs.Ride;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {
    @Mock
    private RideMapper rideMapper;
    @Mock
    private RideGrpcClient client;

    @InjectMocks
    private RideServiceImpl rideService;

    @Test
    void getShouldUseGrpcClient() {
        var rideId = 1L;
        var ride = Mockito.mock(Ride.class);
        Mockito.when(client.getRideById(rideId)).thenReturn(ride);

        var rideResponse = Mockito.mock(RideResponse.class);
        Mockito.when(rideMapper.fromRPCDto(ride)).thenReturn(rideResponse);

        var result = rideService.get(rideId);

        assertThat(result)
                .isEqualTo(rideResponse);
        Mockito.verify(client).getRideById(rideId);
        Mockito.verify(rideMapper).fromRPCDto(ride);
    }

    @Test
    void getShouldNotGrpcClientExceptions() {
        var rideId = 1L;
        Mockito.when(client.getRideById(rideId)).thenThrow(new EntityNotFoundException(""));

        assertThatThrownBy(() -> rideService.get(rideId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(client).getRideById(rideId);
    }
}