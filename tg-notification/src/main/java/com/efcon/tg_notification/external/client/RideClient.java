package com.efcon.tg_notification.external.client;

import com.efcon.tg_notification.dto.DriverAssignment;
import com.efcon.tg_notification.dto.RideResponse;
import com.efcon.tg_notification.external.configuration.RideClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ride", path = "/api/v1/rides", configuration = RideClientConfiguration.class)
public interface RideClient {
    @PostMapping("/{rideId}/accept")
    RideResponse acceptRide(@PathVariable Long rideId, @RequestBody DriverAssignment driverAssignment);
}
