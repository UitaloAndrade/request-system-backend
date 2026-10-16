package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.audit;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity.AuditLogJpaEntity;
import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.repository.SpringDataAuditLogRepository;
import com.uitalo.desafiobackend.requestsystem.domain.model.AuditLog;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuditLogPersistenceAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository repository;

    public AuditLogPersistenceAdapter(
            SpringDataAuditLogRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditLog log) {
        repository.save(new AuditLogJpaEntity(log));
    }
}
