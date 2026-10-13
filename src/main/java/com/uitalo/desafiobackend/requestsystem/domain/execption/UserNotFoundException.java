package com.uitalo.desafiobackend.requestsystem.domain.execption;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID id) {
        super("Usuário não encontrado: " + id);
    }
}