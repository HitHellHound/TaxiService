package com.efcon.rating.external.client;

import com.efcon.rating.AbstractIntegrationTest;
import com.efcon.rating.exception.EntityNotFoundException;
import com.efcon.ride.grpc.stubs.Ride;
import com.efcon.ride.grpc.stubs.RideStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RideGrpcClientIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RideGrpcClient rideGrpcClient;

    @Test
    void shouldReturnRideWithCompletedStatus() {
        var rideWithCompletedStatus = 1L;
        var result = rideGrpcClient.getRideById(rideWithCompletedStatus);

        assertThat(result)
                .isNotNull()
                .extracting(Ride::getId, Ride::getStatus)
                .containsExactly(rideWithCompletedStatus, RideStatus.COMPLETED);
    }

    @Test
    void shouldReturnRideWithAcceptedStatus() {
        var rideWithAcceptedStatus = 2L;
        var result = rideGrpcClient.getRideById(rideWithAcceptedStatus);

        assertThat(result)
                .isNotNull()
                .extracting(Ride::getId, Ride::getStatus)
                .containsExactly(rideWithAcceptedStatus, RideStatus.ACCEPTED);
    }

    @Test
    void throwEntityNotFoundExceptionForNonExistingRide() {
        assertThatThrownBy(() -> rideGrpcClient.getRideById(Long.MAX_VALUE))
                .isInstanceOf(EntityNotFoundException.class);
    }
}