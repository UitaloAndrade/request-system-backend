package com.uitalo.desafiobackend.requestsystem.application.usecase.analyst;

import com.uitalo.desafiobackend.requestsystem.domain.model.DecisionType;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.UUID;

public interface DecideSolicitationUseCase {

    Solicitation decide(
            UUID solicitationId,
            UUID analystId,
            DecisionType type,
            String comment
    );
}
