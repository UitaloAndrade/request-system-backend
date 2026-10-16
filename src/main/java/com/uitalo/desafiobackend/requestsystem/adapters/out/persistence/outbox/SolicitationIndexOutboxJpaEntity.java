package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "solicitation_index_outbox")
public class SolicitationIndexOutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "solicitation_id", nullable = false)
    private UUID solicitationId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    protected SolicitationIndexOutboxJpaEntity() {}

    public SolicitationIndexOutboxJpaEntity(UUID solicitationId) {
        this.solicitationId = solicitationId;
        this.eventType = "UPSERT";
        this.createdAt = Instant.now();
        this.attempts = 0;
    }

    public void markProcessed(Instant processedAt) {
        this.processedAt = processedAt;
        this.lastError = null;
        this.nextAttemptAt = null;
    }

    public void registerFailure(String error) {
        this.attempts++;
        this.lastError = error == null ? "Falha não especificada"
                : error.substring(0, Math.min(error.length(), 2000));
        long delaySeconds = Math.min(300L, 1L << Math.min(this.attempts, 8));
        this.nextAttemptAt = Instant.now().plusSeconds(delaySeconds);
    }

    public Long getId() { return id; }
    public UUID getSolicitationId() { return solicitationId; }
    public int getAttempts() { return attempts; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getProcessedAt() { return processedAt; }
}
