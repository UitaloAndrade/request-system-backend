package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity;

import com.uitalo.desafiobackend.requestsystem.domain.model.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "solicitations")
public class SolicitationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type")
    private ServiceType serviceType;

    @Column(name = "title", length = 80)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "cep", length = 8)
    private String cep;

    @Column(name = "street")
    private String street;

    @Column(name = "city", length = 150)
    private String city;

    @Column(name = "neighborhood", length = 150)
    private String neighborhood;

    @Column(name = "state", length = 2)
    private String state;

    @Column(name = "address_number", length = 20)
    private String addressNumber;

    @Column(name = "complement")
    private String complement;

    @Column(name = "postal_code_validated")
    private Boolean postalCodeValidated;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private Priority priority;

    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "terms_accepted")
    private Boolean termsAccepted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SolicitationStatus status;

    @Column(name = "current_step", nullable = false)
    private int currentStep;

    @Column(name = "analyst_id")
    private UUID analystId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", length = 20)
    private DecisionType decisionType;

    @Column(name = "decision_comment", length = 1000)
    private String decisionComment;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    protected SolicitationJpaEntity() {
        // Construtor exigido pelo JPA.
    }

    public static SolicitationJpaEntity fromDomain(
            Solicitation solicitation
    ) {
        SolicitationJpaEntity entity = new SolicitationJpaEntity();

        entity.id = solicitation.getId();
        entity.clientId = solicitation.getClientId();

        entity.serviceType = solicitation.getServiceType();
        entity.title = solicitation.getTitle();
        entity.description = solicitation.getDescription();

        Address address = solicitation.getAddress();

        if (address != null) {
            entity.cep = address.cep();
            entity.street = address.street();
            entity.city = address.city();
            entity.neighborhood = address.neighborhood();
            entity.state = address.state();
            entity.addressNumber = address.number();
            entity.complement = address.complement();
            entity.postalCodeValidated =
                    address.postalCodeValidated();
        }

        entity.priority = solicitation.getPriority();
        entity.preferredDate = solicitation.getPreferredDate();
        entity.estimatedValue = solicitation.getEstimatedValue();
        entity.termsAccepted = solicitation.getTermsAccepted();

        entity.status = solicitation.getStatus();
        entity.currentStep = solicitation.getCurrentStep();

        entity.analystId = solicitation.getAnalystId();

        Decision decision = solicitation.getDecision();

        if (decision != null) {
            entity.decisionType = decision.type();
            entity.decisionComment = decision.comment();
            entity.decidedAt = decision.decidedAt();
        }

        entity.createdAt = solicitation.getCreatedAt();
        entity.updatedAt = solicitation.getUpdatedAt();
        entity.submittedAt = solicitation.getSubmittedAt();

        return entity;
    }

    public Solicitation toDomain() {
        Address address = null;

        boolean hasAddressData =
                cep != null
                        || street != null
                        || city != null
                        || neighborhood != null
                        || state != null
                        || addressNumber != null
                        || complement != null
                        || postalCodeValidated != null;

        if (hasAddressData) {
            address = new Address(
                    cep,
                    street,
                    city,
                    neighborhood,
                    state,
                    addressNumber,
                    complement,
                    Boolean.TRUE.equals(postalCodeValidated)
            );
        }

        Decision decision = null;

        if (decisionType != null) {
            decision = new Decision(
                    analystId,
                    decisionType,
                    decisionComment,
                    decidedAt
            );
        }

        return Solicitation.reconstitute(
                id,
                clientId,
                createdAt,
                serviceType,
                title,
                description,
                address,
                priority,
                preferredDate,
                estimatedValue,
                termsAccepted,
                status,
                currentStep,
                analystId,
                decision,
                updatedAt,
                submittedAt
        );
    }
}