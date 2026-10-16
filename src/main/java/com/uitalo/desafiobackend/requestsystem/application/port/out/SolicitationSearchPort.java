package com.uitalo.desafiobackend.requestsystem.application.port.out;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SolicitationSearchPort {

    SearchPage search(SearchCriteria criteria);

    record SearchCriteria(
            String q,
            String status,
            String serviceType,
            String priority,
            LocalDate dateFrom,
            LocalDate dateTo,
            List<String> allowedStates,
            int page,
            int size,
            String sortField,
            String sortDirection
    ) {}

    record SearchPage(
            List<SearchItem> items,
            int page,
            int size,
            long total
    ) {}

    record SearchItem(
            UUID id,
            UUID clientId,
            String serviceType,
            String title,
            String description,
            String status,
            String priority,
            String cep,
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state,
            LocalDate preferredDate,
            BigDecimal estimatedValue,
            Instant createdAt,
            Instant updatedAt,
            Instant submittedAt
    ) {}
}
