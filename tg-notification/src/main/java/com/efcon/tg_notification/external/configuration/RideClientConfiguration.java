package com.efcon.tg_notification.external.configuration;

import com.efcon.tg_notification.external.decoder.RideClientErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

public class RideClientConfiguration {
    @Bean
    public ErrorDecoder passengerErrorDecoder() {
        return new RideClientErrorDecoder();
    }
}
