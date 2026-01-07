package com.efcon.ride.external.config;

import com.efcon.ride.external.decoder.PassengerClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class PassengerClientConfiguration {
    @Bean
    public ErrorDecoder passengerErrorDecoder() {
        return new PassengerClientErrorDecoder();
    }
}
