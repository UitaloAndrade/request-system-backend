package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.SubmitSolicitationUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SubmitSolicitationService implements SubmitSolicitationUseCase {

    private final SolicitationRepository solicitationRepository;

    public SubmitSolicitationService(
            SolicitationRepository solicitationRepository) {
        this.solicitationRepository = solicitationRepository;
    }

    @Override
    @Transactional
    public Solicitation submit(UUID solicitationId, UUID clientId) {

        Solicitation solicitation = solicitationRepository
                .findByIdAndClientId(solicitationId, clientId)
                .orElseThrow(() -> new SolicitationNotFoundException(
                        "Solicitação não encontrada para o cliente informado."
                ));

        solicitation.submit(Instant.now());

        return solicitationRepository.save(solicitation);
    }
}
