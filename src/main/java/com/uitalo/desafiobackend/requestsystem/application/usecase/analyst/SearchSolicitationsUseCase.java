package com.uitalo.desafiobackend.requestsystem.application.usecase.analyst;

import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationSearchPort;

import java.time.LocalDate;
import java.util.UUID;

public interface SearchSolicitationsUseCase {

    SolicitationSearchPort.SearchPage search(
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
    );
}
