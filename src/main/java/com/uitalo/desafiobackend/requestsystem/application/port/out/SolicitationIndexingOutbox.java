package com.uitalo.desafiobackend.requestsystem.application.port.out;

import java.util.UUID;

/**
 * Registra uma solicitação para indexação na mesma transação do PostgreSQL.
 */
public interface SolicitationIndexingOutbox {
    void enqueue(UUID solicitationId);
}
