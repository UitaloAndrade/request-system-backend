package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.audit.Audited;
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
    @Audited(
            action = "SOLICITATION_SUBMIT",
            resourceType = "Solicitation",
            resourceIdParameter = 0
    )
    public Solicitation submit(UUID solicitationId, UUID clientId) {
        return submit(solicitationId, clientId, false);
    }

    @Override
    @Transactional
    @Audited(
            action = "SOLICITATION_SUBMIT",
            resourceType = "Solicitation",
            resourceIdParameter = 0
    )
    public Solicitation submit(UUID solicitationId, UUID clientId, boolean administrator) {
        Solicitation solicitation = (administrator
                ? solicitationRepository.findById(solicitationId)
                : solicitationRepository.findByIdAndClientId(solicitationId, clientId))
                .orElseThrow(() -> new SolicitationNotFoundException(
                        administrator ? "Solicitação não encontrada."
                                : "Solicitação não encontrada para o cliente informado."
                ));

        solicitation.submit(Instant.now());
        return solicitationRepository.save(solicitation);
    }
}
