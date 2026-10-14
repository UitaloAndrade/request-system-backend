package com.uitalo.desafiobackend.requestsystem.application.usecase.user;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;

public interface RegisterClientUseCase {

    User register(
            String name,
            String email,
            String rawPassword
    );
}
