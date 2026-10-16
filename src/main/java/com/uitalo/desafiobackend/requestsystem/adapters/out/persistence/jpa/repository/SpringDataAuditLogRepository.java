package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.repository;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataAuditLogRepository
        extends JpaRepository<AuditLogJpaEntity, UUID> {
}