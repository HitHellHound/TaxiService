package com.efcon.ride.external.decoder;

import com.efcon.ride.exception.EntityNotFoundException;
import com.efcon.ride.exception.ExternalBadRequestException;
import com.efcon.ride.exception.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class PassengerClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        String message;
        try (InputStream bodyStream = response.body().asInputStream()) {
            message = new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ExternalServiceException("Passenger service can't process request");
        }

        return switch (response.status()) {
            case 400 -> new ExternalBadRequestException(message);
            case 404 -> new EntityNotFoundException(message);
            default -> new ExternalServiceException("Passenger service can't process request");
        };
    }
}
