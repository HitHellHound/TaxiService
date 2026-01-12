package com.efcon.tg_notification.external.decoder;

import com.efcon.tg_notification.exception.EntityNotFoundException;
import com.efcon.tg_notification.exception.ExternalBadRequestException;
import com.efcon.tg_notification.exception.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RideClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        String message;
        try (InputStream bodyStream = response.body().asInputStream()) {
            message = new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ExternalServiceException("Driver service can't process request");
        }

        return switch (response.status()) {
            case 400 -> new ExternalBadRequestException(message);
            case 404 -> new EntityNotFoundException(message);
            default -> new ExternalServiceException("Driver service can't process request");
        };
    }
}
