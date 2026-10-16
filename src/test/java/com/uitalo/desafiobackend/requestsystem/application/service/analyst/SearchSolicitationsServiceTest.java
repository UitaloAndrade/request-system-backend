package com.uitalo.desafiobackend.requestsystem.application.service.analyst;

import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationSearchPort;
import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchSolicitationsServiceTest {

    @Mock private SolicitationSearchPort searchPort;
    @Mock private AnalystCoverageRepository coverageRepository;

    private SearchSolicitationsService service;
    private UUID analystId;
    private SolicitationSearchPort.SearchPage expectedPage;

    @BeforeEach
    void setUp() {
        service = new SearchSolicitationsService(searchPort, coverageRepository);
        analystId = UUID.randomUUID();
        expectedPage = new SolicitationSearchPort.SearchPage(List.of(), 0, 20, 0);
        lenient().when(coverageRepository.findByAnalystId(analystId)).thenReturn(List.of(
                new AnalystCoverage(analystId, "DF"),
                new AnalystCoverage(analystId, "GO"),
                new AnalystCoverage(analystId, "DF")
        ));
        lenient().when(searchPort.search(any(SolicitationSearchPort.SearchCriteria.class)))
                .thenReturn(expectedPage);
    }

    @Test
    void shouldApplyCoverageAndDefaultSortingForAnalyst() {
        var result = service.search(null, null, null, null, null,
                null, null, 0, 20, null, analystId);

        assertThat(result).isSameAs(expectedPage);
        ArgumentCaptor<SolicitationSearchPort.SearchCriteria> captor =
                ArgumentCaptor.forClass(SolicitationSearchPort.SearchCriteria.class);
        verify(searchPort).search(captor.capture());
        var criteria = captor.getValue();
        assertThat(criteria.allowedStates()).containsExactly("DF", "GO");
        assertThat(criteria.sortField()).isEqualTo("createdAt");
        assertThat(criteria.sortDirection()).isEqualTo("desc");
        verify(coverageRepository).findByAnalystId(analystId);
    }

    @Test
    void shouldNormalizeFiltersAndTrimSearchText() {
        service.search("  vazamento  ", " submitted ", "installation", "high", " df ",
                LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"),
                2, 15, "updatedAt,ASC", analystId);

        verify(searchPort).search(argThat(criteria ->
                "vazamento".equals(criteria.q())
                && "SUBMITTED".equals(criteria.status())
                && "INSTALLATION".equals(criteria.serviceType())
                && "HIGH".equals(criteria.priority())
                && criteria.allowedStates().equals(List.of("DF"))
                && criteria.page() == 2
                && criteria.size() == 15
                && criteria.sortField().equals("updatedAt")
                && criteria.sortDirection().equals("asc")));
    }

    @Test
    void shouldReturnEmptyWhenAnalystHasNoCoverage() {
        when(coverageRepository.findByAnalystId(analystId)).thenReturn(List.of());

        var result = service.search(null, null, null, null, null,
                null, null, 0, 20, "createdAt,desc", analystId);

        assertThat(result.items()).isEmpty();
        assertThat(result.total()).isZero();
        verifyNoInteractions(searchPort);
    }

    @Test
    void shouldReturnEmptyWhenRequestedStateIsOutsideCoverage() {
        var result = service.search(null, null, null, null, "SP",
                null, null, 0, 20, "createdAt,desc", analystId);

        assertThat(result.items()).isEmpty();
        verifyNoInteractions(searchPort);
    }

    @Test
    void administratorCanSearchAllStatesWithoutCoverageLookup() {
        service.search(null, null, null, null, null, null, null,
                0, 20, "createdAt,desc", analystId, true);

        verify(searchPort).search(argThat(criteria -> criteria.allowedStates().isEmpty()));
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void administratorCanApplyAnExplicitStateFilter() {
        service.search(null, null, null, null, " go ", null, null,
                0, 20, "createdAt,desc", analystId, true);

        verify(searchPort).search(argThat(criteria -> criteria.allowedStates().equals(List.of("GO"))));
        verifyNoInteractions(coverageRepository);
    }

    @Test
    void shouldRejectNegativePage() {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null,
                null, null, null, null, -1, 20, null, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 101, Integer.MAX_VALUE})
    void shouldRejectSizeOutsideAllowedRange(int size) {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null,
                null, null, null, null, 0, size, null, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 20, 100})
    void shouldAcceptValidPageSizes(int size) {
        service.search(null, null, null, null, null, null, null,
                0, size, null, analystId);
        verify(searchPort).search(argThat(criteria -> criteria.size() == size));
    }

    @Test
    void shouldRejectDateRangeInReverseOrder() {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null,
                null, null, LocalDate.parse("2026-02-01"), LocalDate.parse("2026-01-01"),
                0, 20, null, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UNKNOWN", "WAITING", "submitted-now"})
    void shouldRejectUnknownStatus(String status) {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, status, null,
                null, null, null, null, 0, 20, null, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"REPAIR", "INSTALL", "install-ation"})
    void shouldRejectUnknownServiceType(String serviceType) {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, serviceType,
                null, null, null, null, 0, 20, null, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"URGENT", "NORMAL", "high-priority"})
    void shouldRejectUnknownPriority(String priority) {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null,
                priority, null, null, null, 0, 20, null, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"D", "123", "D1", "DF/GO"})
    void shouldRejectMalformedState(String state) {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null,
                null, state, null, null, 0, 20, null, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"id,asc", "createdAt,sideways", "createdAt,asc,extra", ",asc"})
    void shouldRejectInvalidSort(String sort) {
        assertThrows(IllegalArgumentException.class, () -> service.search(null, null, null,
                null, null, null, null, 0, 20, sort, analystId));
        verifyNoInteractions(searchPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"createdAt", "updatedAt", "submittedAt", "preferredDate", "status", "priority"})
    void shouldAllowSupportedSortFields(String field) {
        service.search(null, null, null, null, null, null, null,
                0, 20, field + ",desc", analystId);
        verify(searchPort).search(argThat(criteria -> criteria.sortField().equals(field)));
    }
}
