package com.uitalo.desafiobackend.requestsystem.application.service.analyst;

import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationSearchPort;
import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.SearchSolicitationsUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;
import com.uitalo.desafiobackend.requestsystem.domain.model.Priority;
import com.uitalo.desafiobackend.requestsystem.domain.model.ServiceType;
import com.uitalo.desafiobackend.requestsystem.domain.model.SolicitationStatus;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class SearchSolicitationsService implements SearchSolicitationsUseCase {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "submittedAt", "preferredDate",
            "status", "priority"
    );

    private final SolicitationSearchPort searchPort;
    private final AnalystCoverageRepository coverageRepository;

    public SearchSolicitationsService(
            SolicitationSearchPort searchPort,
            AnalystCoverageRepository coverageRepository
    ) {
        this.searchPort = searchPort;
        this.coverageRepository = coverageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitationSearchPort.SearchPage search(
            String q,
            String status,
            String serviceType,
            String priority,
            String state,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size,
            String sort,
            UUID analystId
    ) {
        if (page < 0) {
            throw new IllegalArgumentException("O parâmetro page não pode ser negativo.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("O parâmetro size deve estar entre 1 e 100.");
        }
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("dateFrom não pode ser posterior a dateTo.");
        }

        validateEnum(status, SolicitationStatus.class, "status");
        validateEnum(serviceType, ServiceType.class, "serviceType");
        validateEnum(priority, Priority.class, "priority");

        String normalizedState = normalize(state);
        if (normalizedState != null && !normalizedState.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("state deve conter a sigla de uma UF com duas letras.");
        }
        List<String> allowedStates = coverageRepository.findByAnalystId(analystId)
                .stream()
                .map(AnalystCoverage::state)
                .distinct()
                .toList();

        // A UF enviada pelo cliente nunca substitui a abrangência autorizada.
        if (allowedStates.isEmpty()
                || (normalizedState != null && !allowedStates.contains(normalizedState))) {
            return new SolicitationSearchPort.SearchPage(List.of(), page, size, 0);
        }

        String sortField = "createdAt";
        String sortDirection = "desc";
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.trim().split(",", -1);
            if (parts.length > 2 || parts[0].isBlank()) {
                throw new IllegalArgumentException(
                        "sort deve seguir o formato campo,asc ou campo,desc."
                );
            }
            sortField = parts[0].trim();
            if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
                throw new IllegalArgumentException("Campo de ordenação não permitido.");
            }
            if (parts.length == 2) {
                sortDirection = parts[1].trim().toLowerCase(Locale.ROOT);
            }
            if (!sortDirection.equals("asc") && !sortDirection.equals("desc")) {
                throw new IllegalArgumentException("A direção de sort deve ser asc ou desc.");
            }
        }

        return searchPort.search(new SolicitationSearchPort.SearchCriteria(
                blankToNull(q),
                normalize(status),
                normalize(serviceType),
                normalize(priority),
                dateFrom,
                dateTo,
                normalizedState == null ? allowedStates : List.of(normalizedState),
                page,
                size,
                sortField,
                sortDirection
        ));
    }

    private static <E extends Enum<E>> void validateEnum(
            String value,
            Class<E> enumType,
            String parameter
    ) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Valor inválido para " + parameter + ". Valores permitidos: "
                            + java.util.Arrays.toString(enumType.getEnumConstants())
            );
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
