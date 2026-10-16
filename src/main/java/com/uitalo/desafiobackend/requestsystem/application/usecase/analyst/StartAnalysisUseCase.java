package com.uitalo.desafiobackend.requestsystem.application.usecase.analyst;

import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.UUID;

public interface StartAnalysisUseCase {

    Solicitation start(UUID solicitationId, UUID analystId);
}
