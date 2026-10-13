package com.uitalo.desafiobackend.requestsystem.domain.repository;

import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.util.Optional;
import java.util.UUID;

public interface SolicitationRepository {

    Optional<Solicitation> findById(UUID id);

    Optional<Solicitation> findByIdAndClientId(
            UUID id,
            UUID clientId
    );

    Solicitation save(Solicitation solicitation);
}