package com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation;

import com.uitalo.desafiobackend.requestsystem.domain.model.Priority;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdateSolicitationDetailsUseCase {

    Solicitation update(
            UUID solicitationId,
            UUID clientId,
            Priority priority,
            LocalDate preferredDate,
            BigDecimal estimatedValue,
            Boolean termsAccepted
    );
}
