package com.efcon.rating.service;

import com.efcon.rating.AbstractIntegrationTest;
import com.efcon.rating.dto.RideResponse;
import com.efcon.rating.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RideServiceIntegrationTest extends AbstractIntegrationTest {
    @Autowired
    private RideService rideService;

    @Test
    void shouldGetExistingRide() {
        var rideId = 1L;

        var result = rideService.get(rideId);

        assertThat(result)
                .extracting(RideResponse::id)
                .isEqualTo(rideId);
    }

    @Test
    void throwEntityNotFoundExceptionWhenRideNotExist() {
        var rideId = 10L;

        assertThatThrownBy(() -> rideService.get(rideId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
