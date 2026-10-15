package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.CreateSolicitationUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class CreateSolicitationService implements CreateSolicitationUseCase {

    private final SolicitationRepository solicitationRepository;

    public CreateSolicitationService(
            SolicitationRepository solicitationRepository) {
        this.solicitationRepository = solicitationRepository;
    }

    @Override
    @Transactional
    public Solicitation create(UUID clientId) {
        Objects.requireNonNull(clientId, "O identificador do cliente é obrigatório.");

        Instant now = Instant.now();

        Solicitation solicitation = new Solicitation(
                UUID.randomUUID(),
                clientId,
                now
        );

        return solicitationRepository.save(solicitation);
    }
}