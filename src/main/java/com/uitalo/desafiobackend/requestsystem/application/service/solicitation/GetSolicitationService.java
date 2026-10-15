package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.GetSolicitationUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetSolicitationService implements GetSolicitationUseCase {

    private final SolicitationRepository solicitationRepository;

    public GetSolicitationService(SolicitationRepository solicitationRepository) {
        this.solicitationRepository = solicitationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Solicitation getById(UUID solicitationId, UUID clientId) {
        return solicitationRepository
                .findByIdAndClientId(solicitationId, clientId)
                .orElseThrow(() -> new SolicitationNotFoundException(
                        "Solicitação não encontrada para o cliente informado."
                ));
    }
}