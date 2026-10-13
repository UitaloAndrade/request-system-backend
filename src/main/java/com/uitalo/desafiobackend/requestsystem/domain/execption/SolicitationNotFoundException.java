package com.uitalo.desafiobackend.requestsystem.domain.execption;

import java.util.UUID;

public class SolicitationNotFoundException extends RuntimeException {

    public SolicitationNotFoundException(UUID id) {
        super("Solicitação não encontrada: " + id);
    }
}
