package com.uitalo.desafiobackend.requestsystem.domain.model;

import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Solicitation {

    private final UUID id;
    private final UUID clientId;
    private final Instant createdAt;

    private ServiceType serviceType;
    private String title;
    private String description;

    private Address address;

    private Priority priority;
    private LocalDate preferredDate;
    private BigDecimal estimatedValue;
    private Boolean termsAccepted;

    private SolicitationStatus status;
    private int currentStep;

    private UUID analystId;
    private Decision decision;

    private Instant updatedAt;
    private Instant submittedAt;

    public Solicitation(
            UUID id,
            UUID clientId,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.clientId = Objects.requireNonNull(clientId);
        this.createdAt = Objects.requireNonNull(createdAt);

        this.status = SolicitationStatus.DRAFT;
        this.currentStep = 1;
        this.updatedAt = createdAt;
    }

    public static Solicitation reconstitute(
            UUID id,
            UUID clientId,
            Instant createdAt,
            ServiceType serviceType,
            String title,
            String description,
            Address address,
            Priority priority,
            LocalDate preferredDate,
            BigDecimal estimatedValue,
            Boolean termsAccepted,
            SolicitationStatus status,
            int currentStep,
            UUID analystId,
            Decision decision,
            Instant updatedAt,
            Instant submittedAt
    ) {
        Solicitation solicitation = new Solicitation(
                id,
                clientId,
                createdAt
        );

        solicitation.serviceType = serviceType;
        solicitation.title = title;
        solicitation.description = description;
        solicitation.address = address;
        solicitation.priority = priority;
        solicitation.preferredDate = preferredDate;
        solicitation.estimatedValue = estimatedValue;
        solicitation.termsAccepted = termsAccepted;

        solicitation.status = Objects.requireNonNull(status);
        solicitation.currentStep = currentStep;
        solicitation.analystId = analystId;
        solicitation.decision = decision;

        solicitation.updatedAt = Objects.requireNonNull(updatedAt);
        solicitation.submittedAt = submittedAt;

        return solicitation;
    }


    public void updateBasicData(
            ServiceType serviceType,
            String title,
            String description,
            Instant now
    ) {
        ensureDraft();

        Objects.requireNonNull(serviceType, "O tipo de serviço é obrigatório.");

        String normalizedTitle = normalizeRequired(title, "Título");
        String normalizedDescription =
                normalizeRequired(description, "Descrição");

        if (normalizedTitle.length() < 3
                || normalizedTitle.length() > 80) {
            throw new IllegalArgumentException(
                    "O título deve ter entre 3 e 80 caracteres."
            );
        }

        if (normalizedDescription.length() < 20
                || normalizedDescription.length() > 1000) {
            throw new IllegalArgumentException(
                    "A descrição deve ter entre 20 e 1000 caracteres."
            );
        }

        this.serviceType = serviceType;
        this.title = normalizedTitle;
        this.description = normalizedDescription;

        this.currentStep = Math.max(this.currentStep, 2);
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void updateAddress(Address address, Instant now) {
        ensureDraft();

        Objects.requireNonNull(address, "O endereço é obrigatório.");

        if (!address.isComplete()) {
            throw new IllegalArgumentException(
                    "O endereço está incompleto ou o CEP não foi validado."
            );
        }

        this.address = address;
        this.currentStep = Math.max(this.currentStep, 3);
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void confirmDetails(
            Priority priority,
            LocalDate preferredDate,
            BigDecimal estimatedValue,
            Boolean termsAccepted,
            LocalDate today,
            Instant now
    ) {
        ensureDraft();

        Objects.requireNonNull(priority, "A prioridade é obrigatória.");
        Objects.requireNonNull(preferredDate, "A data preferencial é obrigatória.");
        Objects.requireNonNull(estimatedValue, "O valor estimado é obrigatório.");
        Objects.requireNonNull(today, "A data atual é obrigatória.");

        if (preferredDate.isBefore(today)) {
            throw new IllegalArgumentException(
                    "A data preferencial não pode estar no passado."
            );
        }

        if (estimatedValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "O valor estimado não pode ser negativo."
            );
        }

        if (priority == Priority.HIGH
                && estimatedValue.compareTo(
                new BigDecimal("100")) < 0) {
            throw new IllegalArgumentException(
                    "Solicitações de alta prioridade exigem valor estimado mínimo de 100."
            );
        }

        if (!Boolean.TRUE.equals(termsAccepted)) {
            throw new IllegalArgumentException(
                    "É necessário aceitar os termos."
            );
        }

        this.priority = priority;
        this.preferredDate = preferredDate;
        this.estimatedValue = estimatedValue;
        this.termsAccepted = true;

        this.updatedAt = Objects.requireNonNull(now);
    }

    public void submit(Instant now) {
        ensureDraft();
        validateComplete();

        this.status = SolicitationStatus.SUBMITTED;
        this.submittedAt = Objects.requireNonNull(now);
        this.updatedAt = now;
    }

    public void startReview(UUID analystId, Instant now) {
        if (this.status != SolicitationStatus.SUBMITTED) {
            throw new IllegalStateException(
                    "Somente solicitações enviadas podem entrar em análise."
            );
        }

        this.analystId = Objects.requireNonNull(analystId);
        this.status = SolicitationStatus.IN_REVIEW;
        this.updatedAt = Objects.requireNonNull(now);
    }

    public void decide(Decision decision, Instant now) {
        if (this.status != SolicitationStatus.IN_REVIEW) {
            throw new IllegalStateException(
                    "Somente solicitações em análise podem receber uma decisão."
            );
        }

        Objects.requireNonNull(decision, "A decisão é obrigatória.");

        if (!decision.analystId().equals(this.analystId)) {
            throw new IllegalArgumentException(
                    "A decisão deve ser realizada pelo analista responsável."
            );
        }

        this.decision = decision;
        this.status = decision.type() == DecisionType.APPROVE
                ? SolicitationStatus.APPROVED
                : SolicitationStatus.REJECTED;

        this.updatedAt = Objects.requireNonNull(now);
    }

    private void validateComplete() {
        if (serviceType == null) {
            throw new BusinessRuleException("Etapa 1: tipo de serviço não informado.");
        }

        if (title == null || description == null) {
            throw new BusinessRuleException("Etapa 1: dados básicos incompletos.");
        }

        if (address == null || !address.isComplete()) {
            throw new BusinessRuleException("Etapa 2: endereço ou validação do CEP incompletos.");
        }

        if (priority == null
                || preferredDate == null
                || estimatedValue == null
                || !Boolean.TRUE.equals(termsAccepted)) {
            throw new BusinessRuleException("Etapa 3: confirmação incompleta.");
        }
    }

    private void ensureDraft() {
        if (this.status != SolicitationStatus.DRAFT) {
            throw new IllegalStateException(
                    "A solicitação só pode ser alterada enquanto estiver em rascunho."
            );
        }
    }

    private static String normalizeRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " é obrigatório."
            );
        }

        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Address getAddress() {
        return address;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public Boolean getTermsAccepted() {
        return termsAccepted;
    }

    public SolicitationStatus getStatus() {
        return status;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public UUID getAnalystId() {
        return analystId;
    }

    public Decision getDecision() {
        return decision;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}