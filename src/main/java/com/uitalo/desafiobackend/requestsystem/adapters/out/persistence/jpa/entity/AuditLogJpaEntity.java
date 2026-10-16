package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity;

import com.uitalo.desafiobackend.requestsystem.domain.model.AuditLog;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLogJpaEntity {

    @Id
    private UUID id;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "actor", length = 254)
    private String actor;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "outcome", nullable = false, length = 20)
    private String outcome;

    @Column(name = "details", length = 1000)
    private String details;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditLogJpaEntity() {
    }

    public AuditLogJpaEntity(AuditLog log) {
        this.id = log.id();
        this.actorId = log.actorId();
        this.actor = log.actor();
        this.action = log.action();
        this.resourceType = log.resourceType();
        this.resourceId = log.resourceId();
        this.outcome = log.outcome();
        this.details = log.details();
        this.occurredAt = log.occurredAt();
    }
}
