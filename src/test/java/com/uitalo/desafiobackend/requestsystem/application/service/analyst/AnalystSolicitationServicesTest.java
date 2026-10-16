package com.uitalo.desafiobackend.requestsystem.application.service.analyst;

import com.uitalo.desafiobackend.requestsystem.domain.execption.AnalystCoverageDeniedException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.*;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalystSolicitationServicesTest {

    @Mock private SolicitationRepository solicitationRepository;
    @Mock private AnalystCoverageRepository coverageRepository;

    private UUID solicitationId;
    private UUID analystId;
    private UUID anotherAnalystId;

    @BeforeEach
    void setUp() {
        solicitationId = UUID.randomUUID();
        analystId = UUID.randomUUID();
        anotherAnalystId = UUID.randomUUID();
    }

    @Test
    void analystCanReadRequestWhenCovered() {
        Solicitation solicitation = solicitation(SolicitationStatus.SUBMITTED, null, true);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(true);

        var result = new GetSolicitationForAnalysisService(solicitationRepository, coverageRepository)
                .getById(solicitationId, analystId);

        assertThat(result).isSameAs(solicitation);
    }

    @Test
    void analystCannotReadRequestOutsideCoverage() {
        when(solicitationRepository.findById(solicitationId))
                .thenReturn(Optional.of(solicitation(SolicitationStatus.SUBMITTED, null, true)));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(false);

        assertThrows(AnalystCoverageDeniedException.class,
                () -> new GetSolicitationForAnalysisService(solicitationRepository, coverageRepository)
                        .getById(solicitationId, analystId));
    }

    @Test
    void analystCannotReadRequestWithoutAddressState() {
        when(solicitationRepository.findById(solicitationId))
                .thenReturn(Optional.of(solicitation(SolicitationStatus.SUBMITTED, null, false)));

        assertThrows(AnalystCoverageDeniedException.class,
                () -> new GetSolicitationForAnalysisService(solicitationRepository, coverageRepository)
                        .getById(solicitationId, analystId));
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void administratorCanReadRequestWithoutCoverageOrAddress() {
        Solicitation solicitation = solicitation(SolicitationStatus.SUBMITTED, null, false);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));

        assertThat(new GetSolicitationForAnalysisService(solicitationRepository, coverageRepository)
                .getById(solicitationId, analystId, true)).isSameAs(solicitation);
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void readingMissingRequestThrowsNotFound() {
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.empty());
        assertThrows(SolicitationNotFoundException.class,
                () -> new GetSolicitationForAnalysisService(solicitationRepository, coverageRepository)
                        .getById(solicitationId, analystId));
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void analystCanStartAnalysisWhenCovered() {
        Solicitation solicitation = solicitation(SolicitationStatus.SUBMITTED, null, true);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(true);
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        var result = new StartAnalysisService(solicitationRepository, coverageRepository)
                .start(solicitationId, analystId);

        assertThat(result.getStatus()).isEqualTo(SolicitationStatus.IN_REVIEW);
        assertThat(result.getAnalystId()).isEqualTo(analystId);
        verify(solicitationRepository).save(solicitation);
    }

    @Test
    void analystCannotStartAnalysisOutsideCoverage() {
        when(solicitationRepository.findById(solicitationId))
                .thenReturn(Optional.of(solicitation(SolicitationStatus.SUBMITTED, null, true)));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(false);

        assertThrows(AnalystCoverageDeniedException.class,
                () -> new StartAnalysisService(solicitationRepository, coverageRepository)
                        .start(solicitationId, analystId));
        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void administratorCanStartAnalysisWithoutAddressOrCoverage() {
        Solicitation solicitation = solicitation(SolicitationStatus.SUBMITTED, null, false);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        var result = new StartAnalysisService(solicitationRepository, coverageRepository)
                .start(solicitationId, analystId, true);

        assertThat(result.getStatus()).isEqualTo(SolicitationStatus.IN_REVIEW);
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void cannotStartAnalysisWhenRequestIsNotSubmitted() {
        when(solicitationRepository.findById(solicitationId))
                .thenReturn(Optional.of(solicitation(SolicitationStatus.DRAFT, null, true)));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> new StartAnalysisService(solicitationRepository, coverageRepository)
                        .start(solicitationId, analystId));
        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void analystCanDecideOwnRequestWhenCovered() {
        Solicitation solicitation = solicitation(SolicitationStatus.IN_REVIEW, analystId, true);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(true);
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        var result = new DecideSolicitationService(solicitationRepository, coverageRepository)
                .decide(solicitationId, analystId, DecisionType.APPROVE, "Solicitação aprovada.");

        assertThat(result.getStatus()).isEqualTo(SolicitationStatus.APPROVED);
        assertThat(result.getDecision().analystId()).isEqualTo(analystId);
    }

    @Test
    void analystCannotDecideRequestOutsideCoverage() {
        when(solicitationRepository.findById(solicitationId))
                .thenReturn(Optional.of(solicitation(SolicitationStatus.IN_REVIEW, analystId, true)));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(false);

        assertThrows(AnalystCoverageDeniedException.class,
                () -> new DecideSolicitationService(solicitationRepository, coverageRepository)
                        .decide(solicitationId, analystId, DecisionType.REJECT, "Motivo suficiente."));
        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void analystCannotDecideRequestAssignedToAnotherAnalyst() {
        when(solicitationRepository.findById(solicitationId))
                .thenReturn(Optional.of(solicitation(SolicitationStatus.IN_REVIEW, anotherAnalystId, true)));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> new DecideSolicitationService(solicitationRepository, coverageRepository)
                        .decide(solicitationId, analystId, DecisionType.REJECT, "Motivo suficiente."));
        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void administratorCanDecideRequestAssignedToAnotherAnalyst() {
        Solicitation solicitation = solicitation(SolicitationStatus.IN_REVIEW, anotherAnalystId, false);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        var result = new DecideSolicitationService(solicitationRepository, coverageRepository)
                .decide(solicitationId, analystId, DecisionType.REJECT, "Rejeitada pelo administrador.", true);

        assertThat(result.getStatus()).isEqualTo(SolicitationStatus.REJECTED);
        assertThat(result.getDecision().analystId()).isEqualTo(analystId);
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void decisionCommentMustRespectDomainLimits() {
        Solicitation solicitation = solicitation(SolicitationStatus.IN_REVIEW, analystId, true);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> new DecideSolicitationService(solicitationRepository, coverageRepository)
                        .decide(solicitationId, analystId, DecisionType.REJECT, "curto"));
        verify(solicitationRepository, never()).save(any());
    }

    private static Solicitation solicitation(SolicitationStatus status, UUID assignedAnalyst, boolean withAddress) {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        Address address = withAddress
                ? new Address("70040010", "Esplanada dos Ministérios", "Brasília",
                    "Zona Cívico-Administrativa", "DF", "10", null, true)
                : null;
        return Solicitation.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), now,
                ServiceType.INSTALLATION, "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado.",
                address, Priority.MEDIUM, LocalDate.now().plusDays(2),
                new BigDecimal("150.00"), true, status, 3, assignedAnalyst,
                null, now, status == SolicitationStatus.DRAFT ? null : now);
    }
}
