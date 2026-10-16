package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationSearchPort;
import com.uitalo.desafiobackend.requestsystem.application.service.analyst.DecideSolicitationService;
import com.uitalo.desafiobackend.requestsystem.application.service.analyst.GetSolicitationForAnalysisService;
import com.uitalo.desafiobackend.requestsystem.application.service.analyst.SearchSolicitationsService;
import com.uitalo.desafiobackend.requestsystem.application.service.analyst.StartAnalysisService;
import com.uitalo.desafiobackend.requestsystem.domain.execption.AnalystCoverageDeniedException;
import com.uitalo.desafiobackend.requestsystem.domain.model.*;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSolicitationPermissionsTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @Mock
    private AnalystCoverageRepository coverageRepository;

    @Mock
    private SolicitationSearchPort searchPort;

    @Test
    void administratorCanReadSolicitationOutsideCoverage() {
        UUID solicitationId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Solicitation solicitation = solicitationWithStatus(SolicitationStatus.SUBMITTED,
                UUID.randomUUID());
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));

        GetSolicitationForAnalysisService service = new GetSolicitationForAnalysisService(
                solicitationRepository, coverageRepository);

        assertThat(service.getById(solicitationId, adminId, true)).isSameAs(solicitation);
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void analystStillCannotReadSolicitationOutsideCoverage() {
        UUID solicitationId = UUID.randomUUID();
        UUID analystId = UUID.randomUUID();
        Solicitation solicitation = solicitationWithStatus(SolicitationStatus.SUBMITTED,
                UUID.randomUUID());
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(coverageRepository.existsByAnalystIdAndState(analystId, "DF")).thenReturn(false);

        GetSolicitationForAnalysisService service = new GetSolicitationForAnalysisService(
                solicitationRepository, coverageRepository);

        assertThrows(AnalystCoverageDeniedException.class,
                () -> service.getById(solicitationId, analystId));
    }

    @Test
    void administratorCanStartAnalysisWithoutCoverage() {
        UUID solicitationId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Solicitation solicitation = solicitationWithStatus(SolicitationStatus.SUBMITTED, null);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        StartAnalysisService service = new StartAnalysisService(solicitationRepository, coverageRepository);

        Solicitation result = service.start(solicitationId, adminId, true);

        assertThat(result.getStatus()).isEqualTo(SolicitationStatus.IN_REVIEW);
        assertThat(result.getAnalystId()).isEqualTo(adminId);
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void administratorCanDecideRequestAssignedToAnotherAnalyst() {
        UUID solicitationId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID assignedAnalystId = UUID.randomUUID();
        Solicitation solicitation = solicitationWithStatus(SolicitationStatus.IN_REVIEW, assignedAnalystId);
        when(solicitationRepository.findById(solicitationId)).thenReturn(Optional.of(solicitation));
        when(solicitationRepository.save(solicitation)).thenReturn(solicitation);

        DecideSolicitationService service = new DecideSolicitationService(
                solicitationRepository, coverageRepository);

        Solicitation result = service.decide(solicitationId, adminId, DecisionType.APPROVE,
                "Aprovado pelo administrador", true);

        assertThat(result.getStatus()).isEqualTo(SolicitationStatus.APPROVED);
        assertThat(result.getDecision().analystId()).isEqualTo(adminId);
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void administratorSearchesAcrossAllStatesWhenStateIsNotProvided() {
        UUID adminId = UUID.randomUUID();
        SolicitationSearchPort.SearchPage expected = new SolicitationSearchPort.SearchPage(
                List.of(), 0, 20, 0);
        when(searchPort.search(any(SolicitationSearchPort.SearchCriteria.class))).thenReturn(expected);

        SearchSolicitationsService service = new SearchSolicitationsService(searchPort, coverageRepository);
        SolicitationSearchPort.SearchPage result = service.search(
                null, null, null, null, null, null, null,
                0, 20, "createdAt,desc", adminId, true);

        assertThat(result).isSameAs(expected);
        verify(searchPort).search(argThat(criteria -> criteria.allowedStates().isEmpty()));
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void analystSearchRemainsRestrictedToConfiguredCoverage() {
        UUID analystId = UUID.randomUUID();
        SolicitationSearchPort.SearchPage expected = new SolicitationSearchPort.SearchPage(
                List.of(), 0, 20, 0);
        when(coverageRepository.findByAnalystId(analystId))
                .thenReturn(List.of(new AnalystCoverage(analystId, "DF")));
        when(searchPort.search(any(SolicitationSearchPort.SearchCriteria.class))).thenReturn(expected);

        SearchSolicitationsService service = new SearchSolicitationsService(searchPort, coverageRepository);
        SolicitationSearchPort.SearchPage result = service.search(
                null, null, null, null, null, null, null,
                0, 20, "createdAt,desc", analystId);

        assertThat(result).isSameAs(expected);
        verify(searchPort).search(argThat(criteria -> criteria.allowedStates().equals(List.of("DF"))));
    }

    private static Solicitation solicitationWithStatus(SolicitationStatus status, UUID analystId) {
        Instant now = Instant.now();
        return Solicitation.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), now,
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado.",
                new Address("70040010", "Esplanada dos Ministérios", "Brasília",
                        "Zona Cívico-Administrativa", "DF", "10", null, true),
                Priority.MEDIUM,
                java.time.LocalDate.now().plusDays(2),
                new java.math.BigDecimal("150.00"),
                true,
                status,
                3,
                analystId,
                null,
                now,
                now
        );
    }
}
