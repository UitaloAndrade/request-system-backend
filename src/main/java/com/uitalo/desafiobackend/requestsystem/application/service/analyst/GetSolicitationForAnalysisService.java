package com.uitalo.desafiobackend.requestsystem.application.service.analyst;

import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.GetSolicitationForAnalysisUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.AnalystCoverageDeniedException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetSolicitationForAnalysisService
        implements GetSolicitationForAnalysisUseCase {

    private final SolicitationRepository solicitationRepository;
    private final AnalystCoverageRepository analystCoverageRepository;

    public GetSolicitationForAnalysisService(
            SolicitationRepository solicitationRepository,
            AnalystCoverageRepository analystCoverageRepository
    ) {
        this.solicitationRepository = solicitationRepository;
        this.analystCoverageRepository = analystCoverageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Solicitation getById(UUID solicitationId, UUID analystId) {
        Solicitation solicitation = solicitationRepository.findById(solicitationId)
                .orElseThrow(() ->
                        new SolicitationNotFoundException(
                                "Solicitação não encontrada."
                        )
                );

        validateCoverage(solicitation, analystId);

        return solicitation;
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
