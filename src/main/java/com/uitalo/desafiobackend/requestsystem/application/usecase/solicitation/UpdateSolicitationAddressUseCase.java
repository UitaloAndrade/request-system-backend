package com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation;

import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.UUID;

public interface UpdateSolicitationAddressUseCase {

    Solicitation update(
            UUID solicitationId,
            UUID clientId,
            String cep,
            String number,
            String complement
    );
}