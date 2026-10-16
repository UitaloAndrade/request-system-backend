package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.audit.Audited;
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
    @Audited(
            action = "SOLICITATION_BASIC_DATA_UPDATED",
            resourceType = "Solicitation",
            resourceIdParameter = 0
    )
    public Solicitation update(
            UUID solicitationId,
            UUID clientId,
            ServiceType serviceType,
            String title,
            String description) {

        return update(solicitationId, clientId, serviceType, title, description, false);
    }

    @Override
    @Transactional
    @Audited(
            action = "SOLICITATION_BASIC_DATA_UPDATED",
            resourceType = "Solicitation",
            resourceIdParameter = 0
    )
    public Solicitation update(
            UUID solicitationId,
            UUID clientId,
            ServiceType serviceType,
            String title,
            String description,
            boolean administrator) {
        Solicitation solicitation = (administrator
                ? solicitationRepository.findById(solicitationId)
                : solicitationRepository.findByIdAndClientId(solicitationId, clientId))
                .orElseThrow(() -> new SolicitationNotFoundException(
                        administrator ? "Solicitação não encontrada."
                                : "Solicitação não encontrada para o cliente informado."
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
