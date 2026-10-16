package com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {
        write(request, response, status, code, message, Map.of());
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message,
            Map<String, String> details
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.of(
                status,
                code,
                message,
                request.getRequestURI(),
                details
        );

        // Serializa timestamp como ISO-8601 para não depender de módulos de data/hora
        // na instância do serializador usada pelo filtro.
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("timestamp", body.timestamp().toString());
        json.put("status", body.status());
        json.put("error", body.error());
        json.put("code", body.code());
        json.put("message", body.message());
        json.put("path", body.path());
        json.put("details", body.details());
        objectMapper.writeValue(response.getOutputStream(), json);
    }
}
