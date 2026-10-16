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
        return getById(solicitationId, clientId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Solicitation getById(UUID solicitationId, UUID clientId, boolean administrator) {
        var result = administrator
                ? solicitationRepository.findById(solicitationId)
                : solicitationRepository.findByIdAndClientId(solicitationId, clientId);
        return result.orElseThrow(() -> new SolicitationNotFoundException(
                administrator
                        ? "Solicitação não encontrada."
                        : "Solicitação não encontrada para o cliente informado."
        ));
    }
}