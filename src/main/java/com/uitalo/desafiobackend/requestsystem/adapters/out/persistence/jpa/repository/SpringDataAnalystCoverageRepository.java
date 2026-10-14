package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.repository;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity.AnalystCoverageJpaEntity;
import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity.AnalystCoverageJpaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAnalystCoverageRepository
        extends JpaRepository<AnalystCoverageJpaEntity, AnalystCoverageJpaId> {

    List<AnalystCoverageJpaEntity> findAllByIdAnalystId(UUID analystId);

    boolean existsByIdAnalystIdAndIdState(
            UUID analystId,
            String state
    );

    void deleteByIdAnalystId(UUID analystId);
}
