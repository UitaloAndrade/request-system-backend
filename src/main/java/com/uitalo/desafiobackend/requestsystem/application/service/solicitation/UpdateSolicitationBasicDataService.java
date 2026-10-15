package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.UpdateSolicitationBasicDataUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.ServiceType;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateSolicitationBasicDataService
        implements UpdateSolicitationBasicDataUseCase {

    private final SolicitationRepository solicitationRepository;

    public UpdateSolicitationBasicDataService(
            SolicitationRepository solicitationRepository) {
        this.solicitationRepository = solicitationRepository;
    }

    @Override
    @Transactional
    public Solicitation update(
            UUID solicitationId,
            UUID clientId,
            ServiceType serviceType,
            String title,
            String description) {

        Solicitation solicitation = solicitationRepository
                .findByIdAndClientId(solicitationId, clientId)
                .orElseThrow(() -> new SolicitationNotFoundException(
                        "Solicitação não encontrada para o cliente informado."
                ));

        solicitation.updateBasicData(
                serviceType,
                title,
                description,
                Instant.now()
        );

        return solicitationRepository.save(solicitation);
    }
}
