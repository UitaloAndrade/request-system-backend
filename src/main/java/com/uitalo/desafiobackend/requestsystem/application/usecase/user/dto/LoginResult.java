package com.uitalo.desafiobackend.requestsystem.application.usecase.user.dto;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;

public record LoginResult(
        User user,
        String accessToken,
        long expiresIn
) {
}