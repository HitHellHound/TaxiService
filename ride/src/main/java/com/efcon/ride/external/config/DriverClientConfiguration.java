package com.efcon.ride.external.config;

import com.efcon.ride.external.decoder.DriverClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class DriverClientConfiguration {
    @Bean
    public ErrorDecoder driverErrorDecoder() {
        return new DriverClientErrorDecoder();
    }
}
