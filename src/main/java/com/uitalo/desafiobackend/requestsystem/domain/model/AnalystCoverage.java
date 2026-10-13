package com.uitalo.desafiobackend.requestsystem.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public record AnalystCoverage(
        UUID analystId,
        String state
) {
    public AnalystCoverage {
        Objects.requireNonNull(analystId, "O analista é obrigatório.");

        if (state == null || !state.trim().matches("[A-Za-z]{2}")) {
            throw new IllegalArgumentException(
                    "A UF deve conter exatamente duas letras."
            );
        }

        state = state.trim().toUpperCase(Locale.ROOT);
    }
}
