package com.uitalo.desafiobackend.requestsystem.application.usecase.analyst;

import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.UUID;

public interface GetSolicitationForAnalysisUseCase {

    Solicitation getById(UUID solicitationId, UUID analystId);

    Solicitation getById(UUID solicitationId, UUID analystId, boolean administrator);
}
