package com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation;

import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.UUID;

public interface GetSolicitationUseCase {

    Solicitation getById(UUID solicitationId, UUID clientId);

    Solicitation getById(UUID solicitationId, UUID clientId, boolean administrator);
}