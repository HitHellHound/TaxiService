package com.efcon.rating.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.ValidatingEntityCallback;

@Configuration
public class ValidationConfig {
    @Bean
    public ValidatingEntityCallback validatingMongoEventListener(Validator validator) {
        return new ValidatingEntityCallback(validator);
    }
}
