package com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}