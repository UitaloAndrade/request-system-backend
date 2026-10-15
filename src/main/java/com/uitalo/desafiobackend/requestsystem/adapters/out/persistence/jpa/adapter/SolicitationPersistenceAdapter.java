package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.adapter;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity.SolicitationJpaEntity;
import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.repository.SolicitationJpaRepository;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class SolicitationPersistenceAdapter
        implements SolicitationRepository {

    private final SolicitationJpaRepository jpaRepository;

    public SolicitationPersistenceAdapter(
            SolicitationJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Solicitation> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(SolicitationJpaEntity::toDomain);
    }

    @Override
    public Optional<Solicitation> findByIdAndClientId(
            UUID id,
            UUID clientId
    ) {
        return jpaRepository.findByIdAndClientId(id, clientId)
                .map(SolicitationJpaEntity::toDomain);
    }

    @Override
    @Transactional
    public Solicitation save(Solicitation solicitation) {
        SolicitationJpaEntity entity =
                SolicitationJpaEntity.fromDomain(solicitation);

        SolicitationJpaEntity savedEntity =
                jpaRepository.save(entity);

        return savedEntity.toDomain();
    }
}
