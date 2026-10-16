package com.uitalo.desafiobackend.requestsystem.application.service.analyst;

import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.StartAnalysisUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.AnalystCoverageDeniedException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class StartAnalysisService implements StartAnalysisUseCase {

    private final SolicitationRepository solicitationRepository;
    private final AnalystCoverageRepository analystCoverageRepository;

    public StartAnalysisService(
            SolicitationRepository solicitationRepository,
            AnalystCoverageRepository analystCoverageRepository
    ) {
        this.solicitationRepository = solicitationRepository;
        this.analystCoverageRepository = analystCoverageRepository;
    }

    @Override
    @Transactional
    public Solicitation start(UUID solicitationId, UUID analystId) {
        return start(solicitationId, analystId, false);
    }

    @Override
    @Transactional
    public Solicitation start(UUID solicitationId, UUID analystId, boolean administrator) {
        Solicitation solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() ->
                        new SolicitationNotFoundException(
                                "Solicitação não encontrada."
                        )
                );

        if (!administrator) {
            validateCoverage(solicitation, analystId);
        }

        // O domínio valida a transição SUBMITTED -> IN_REVIEW.
        solicitation.startReview(analystId, Instant.now());

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
