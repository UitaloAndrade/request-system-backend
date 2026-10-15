package com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation;

import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.UUID;

public interface SubmitSolicitationUseCase {

    Solicitation submit(UUID solicitationId, UUID clientId);
}