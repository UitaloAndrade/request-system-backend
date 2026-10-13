package com.uitalo.desafiobackend.requestsystem.domain.repository;

import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;

import java.util.List;
import java.util.UUID;

public interface AnalystCoverageRepository {

    List<AnalystCoverage> findByAnalystId(UUID analystId);

    boolean existsByAnalystIdAndState(
            UUID analystId,
            String state
    );

    List<AnalystCoverage> saveAll(
            List<AnalystCoverage> coverages
    );

    void deleteByAnalystId(UUID analystId);
}