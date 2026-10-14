package com.uitalo.desafiobackend.requestsystem.application.usecase.user;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;

public interface CreateInternalUserUseCase {

    User create(
            String name,
            String email,
            String rawPassword,
            UserRole role
    );
}