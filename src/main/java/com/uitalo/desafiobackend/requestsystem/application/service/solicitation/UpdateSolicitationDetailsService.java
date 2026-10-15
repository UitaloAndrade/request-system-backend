package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.UpdateSolicitationDetailsUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.Priority;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class UpdateSolicitationDetailsService
        implements UpdateSolicitationDetailsUseCase {

    private final SolicitationRepository solicitationRepository;

    public UpdateSolicitationDetailsService(
            SolicitationRepository solicitationRepository) {
        this.solicitationRepository = solicitationRepository;
    }

    @Override
    @Transactional
    public Solicitation update(
            UUID solicitationId,
            UUID clientId,
            Priority priority,
            LocalDate preferredDate,
            BigDecimal estimatedValue,
            Boolean termsAccepted) {

        Solicitation solicitation = solicitationRepository
                .findByIdAndClientId(solicitationId, clientId)
                .orElseThrow(() -> new SolicitationNotFoundException(
                        "Solicitação não encontrada para o cliente informado."
                ));

        solicitation.confirmDetails(
                priority,
                preferredDate,
                estimatedValue,
                termsAccepted,
                LocalDate.now(),
                Instant.now()
        );

        return solicitationRepository.save(solicitation);
    }
}
