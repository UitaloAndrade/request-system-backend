package com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, String> details
) {
    public ErrorResponse {
        timestamp = timestamp == null ? Instant.now() : timestamp;
        details = details == null || details.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    public static ErrorResponse of(
            HttpStatus status,
            String code,
            String message,
            String path,
            Map<String, String> details
    ) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                details
        );
    }
}
