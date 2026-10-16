package com.uitalo.desafiobackend.requestsystem.application.service.analyst;

import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.DecideSolicitationUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.AnalystCoverageDeniedException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Decision;
import com.uitalo.desafiobackend.requestsystem.domain.model.DecisionType;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class DecideSolicitationService implements DecideSolicitationUseCase {

    private final SolicitationRepository solicitationRepository;
    private final AnalystCoverageRepository analystCoverageRepository;

    public DecideSolicitationService(
            SolicitationRepository solicitationRepository,
            AnalystCoverageRepository analystCoverageRepository
    ) {
        this.solicitationRepository = solicitationRepository;
        this.analystCoverageRepository = analystCoverageRepository;
    }

    @Override
    @Transactional
    public Solicitation decide(
            UUID solicitationId,
            UUID analystId,
            DecisionType type,
            String comment
    ) {
        Solicitation solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() ->
                        new SolicitationNotFoundException(
                                "Solicitação não encontrada."
                        )
                );

        validateCoverage(solicitation, analystId);

        Decision decision = new Decision(
                analystId,
                type,
                comment,
                Instant.now()
        );

        // O domínio verifica o status e se o analista é o responsável.
        solicitation.decide(decision, Instant.now());

        return solicitationRepository.save(solicitation);
    }

    private void validateCoverage(
            Solicitation solicitation,
            UUID analystId
    ) {
        Address address = solicitation.getAddress();

        if (address == null || address.state() == null) {
            throw new AnalystCoverageDeniedException(
                    "Não é possível verificar a cobertura da solicitação."
            );
        }

        boolean hasCoverage =
                analystCoverageRepository.existsByAnalystIdAndState(
                        analystId,
                        address.state()
                );

        if (!hasCoverage) {
            throw new AnalystCoverageDeniedException(
                    "Você não possui cobertura para a UF desta solicitação."
            );
        }
    }
}
