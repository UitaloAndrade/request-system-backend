package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.outbox;

import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationIndexingOutbox;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public class SolicitationIndexingOutboxPersistenceAdapter
        implements SolicitationIndexingOutbox {

    private final SpringDataSolicitationIndexOutboxRepository repository;

    public SolicitationIndexingOutboxPersistenceAdapter(
            SpringDataSolicitationIndexOutboxRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(UUID solicitationId) {
        repository.save(new SolicitationIndexOutboxJpaEntity(solicitationId));
    }
}
