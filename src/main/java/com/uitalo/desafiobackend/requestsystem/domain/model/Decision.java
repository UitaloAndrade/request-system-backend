package com.uitalo.desafiobackend.requestsystem.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Decision(
        UUID analystId,
        DecisionType type,
        String comment,
        Instant decidedAt
) {
    public Decision {
        Objects.requireNonNull(analystId, "O analista é obrigatório.");
        Objects.requireNonNull(type, "A decisão é obrigatória.");
        Objects.requireNonNull(decidedAt, "A data da decisão é obrigatória.");

        if (comment == null) {
            throw new IllegalArgumentException(
                    "O comentário da decisão é obrigatório."
            );
        }

        comment = comment.trim();

        if (comment.length() < 10 || comment.length() > 1000) {
            throw new IllegalArgumentException(
                    "O comentário deve ter entre 10 e 1000 caracteres."
            );
        }
    }
}