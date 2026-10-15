package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.repository;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity.SolicitationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SolicitationJpaRepository
        extends JpaRepository<SolicitationJpaEntity, UUID> {

    Optional<SolicitationJpaEntity> findByIdAndClientId(
            UUID id,
            UUID clientId
    );
}