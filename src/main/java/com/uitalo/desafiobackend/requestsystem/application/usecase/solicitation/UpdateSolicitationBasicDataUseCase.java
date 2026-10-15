package com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation;

import com.uitalo.desafiobackend.requestsystem.domain.model.ServiceType;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.UUID;

public interface UpdateSolicitationBasicDataUseCase {

    Solicitation update(
            UUID solicitationId,
            UUID clientId,
            ServiceType serviceType,
            String title,
            String description
    );
}
