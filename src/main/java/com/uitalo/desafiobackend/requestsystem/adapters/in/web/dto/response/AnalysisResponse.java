package com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response;

import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Decision;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        UUID clientId,
        String serviceType,
        String title,
        String description,
        Address address,
        String priority,
        LocalDate preferredDate,
        BigDecimal estimatedValue,
        Boolean termsAccepted,
        String status,
        int currentStep,
        UUID analystId,
        DecisionResponse decision,
        Instant createdAt,
        Instant updatedAt,
        Instant submittedAt
) {

    public static AnalysisResponse from(Solicitation solicitation) {
        Decision decision = solicitation.getDecision();

        DecisionResponse decisionResponse = decision == null
                ? null
                : new DecisionResponse(
                decision.analystId(),
                decision.type().name(),
                decision.comment(),
                decision.decidedAt()
        );

        return new AnalysisResponse(
                solicitation.getId(),
                solicitation.getClientId(),
                solicitation.getServiceType() == null
                        ? null
                        : solicitation.getServiceType().name(),
                solicitation.getTitle(),
                solicitation.getDescription(),
                solicitation.getAddress(),
                solicitation.getPriority() == null
                        ? null
                        : solicitation.getPriority().name(),
                solicitation.getPreferredDate(),
                solicitation.getEstimatedValue(),
                solicitation.getTermsAccepted(),
                solicitation.getStatus().name(),
                solicitation.getCurrentStep(),
                solicitation.getAnalystId(),
                decisionResponse,
                solicitation.getCreatedAt(),
                solicitation.getUpdatedAt(),
                solicitation.getSubmittedAt()
        );
    }

    public record DecisionResponse(
            UUID analystId,
            String type,
            String comment,
            Instant decidedAt
    ) {
    }
}
