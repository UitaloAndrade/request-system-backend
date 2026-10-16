package com.uitalo.desafiobackend.requestsystem.domain.model;

import java.time.Instant;
import java.util.UUID;

public record AuditLog(
        UUID id,
        UUID actorId,
        String actor,
        String action,
        String resourceType,
        String resourceId,
        String outcome,
        String details,
        Instant occurredAt
) {
    public AuditLog {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("A ação da auditoria é obrigatória.");
        }

        if (resourceType == null || resourceType.isBlank()) {
            throw new IllegalArgumentException("O tipo do recurso é obrigatório.");
        }

        if (outcome == null || outcome.isBlank()) {
            throw new IllegalArgumentException("O resultado da auditoria é obrigatório.");
        }

        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
