package com.uitalo.desafiobackend.requestsystem.model;

import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import com.uitalo.desafiobackend.requestsystem.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitationDomainRulesTest {

    private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");
    private static final LocalDate TODAY = LocalDate.parse("2026-01-10");

    @Test
    void shouldTrimBasicDataAndAdvanceToStepTwo() {
        Solicitation solicitation = draft();
        solicitation.updateBasicData(ServiceType.INSTALLATION, "  Instalação  ",
                "  Solicito a instalação de um equipamento.  ", NOW.plusSeconds(1));

        assertThat(solicitation.getTitle()).isEqualTo("Instalação");
        assertThat(solicitation.getDescription()).isEqualTo("Solicito a instalação de um equipamento.");
        assertThat(solicitation.getCurrentStep()).isEqualTo(2);
    }

    @Test
    void shouldRejectTitleOutsideAllowedLength() {
        Solicitation solicitation = draft();
        assertThrows(IllegalArgumentException.class, () -> solicitation.updateBasicData(
                ServiceType.INSTALLATION, "AB", "Solicito a instalação de um equipamento.", NOW));
        assertThrows(IllegalArgumentException.class, () -> solicitation.updateBasicData(
                ServiceType.INSTALLATION, "T".repeat(81), "Solicito a instalação de um equipamento.", NOW));
    }

    @Test
    void shouldRejectDescriptionOutsideAllowedLength() {
        Solicitation solicitation = draft();
        assertThrows(IllegalArgumentException.class, () -> solicitation.updateBasicData(
                ServiceType.INSTALLATION, "Instalação", "curta", NOW));
        assertThrows(IllegalArgumentException.class, () -> solicitation.updateBasicData(
                ServiceType.INSTALLATION, "Instalação", "D".repeat(1001), NOW));
    }

    @Test
    void shouldRejectBlankRequiredBasicFields() {
        Solicitation solicitation = draft();
        assertThrows(IllegalArgumentException.class, () -> solicitation.updateBasicData(
                ServiceType.INSTALLATION, "   ", "Solicito a instalação de um equipamento.", NOW));
        assertThrows(IllegalArgumentException.class, () -> solicitation.updateBasicData(
                ServiceType.INSTALLATION, "Instalação", "   ", NOW));
    }

    @Test
    void shouldRejectUpdatingBasicDataAfterDraft() {
        Solicitation solicitation = completeDraft();
        solicitation.submit(NOW.plusSeconds(1));
        assertThrows(IllegalStateException.class, () -> solicitation.updateBasicData(
                ServiceType.MAINTENANCE, "Manutenção", "Solicito a manutenção de um equipamento.", NOW));
    }

    @Test
    void shouldUpdateAddressAndAdvanceToStepThree() {
        Solicitation solicitation = draft();
        solicitation.updateAddress(address(), NOW.plusSeconds(1));
        assertThat(solicitation.getAddress()).isEqualTo(address());
        assertThat(solicitation.getCurrentStep()).isEqualTo(3);
    }

    @Test
    void shouldRejectIncompleteOrUnvalidatedAddress() {
        Solicitation solicitation = draft();
        Address incomplete = new Address("70040010", "Rua", "Brasília", "Centro", "DF", "10", null, false);
        assertThrows(IllegalArgumentException.class, () -> solicitation.updateAddress(incomplete, NOW));
        assertThrows(NullPointerException.class, () -> solicitation.updateAddress(null, NOW));
    }

    @Test
    void shouldAcceptValidConfirmationDetails() {
        Solicitation solicitation = draftWithBasicDataAndAddress();
        solicitation.confirmDetails(Priority.MEDIUM, TODAY, new BigDecimal("99.99"), true, TODAY, NOW);

        assertThat(solicitation.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(solicitation.getPreferredDate()).isEqualTo(TODAY);
        assertThat(solicitation.getEstimatedValue()).isEqualByComparingTo("99.99");
        assertThat(solicitation.getTermsAccepted()).isTrue();
    }

    @Test
    void shouldRejectPreferredDateInPast() {
        Solicitation solicitation = draftWithBasicDataAndAddress();
        assertThrows(IllegalArgumentException.class, () -> solicitation.confirmDetails(
                Priority.LOW, TODAY.minusDays(1), BigDecimal.ONE, true, TODAY, NOW));
    }

    @Test
    void shouldRejectNegativeEstimatedValue() {
        Solicitation solicitation = draftWithBasicDataAndAddress();
        assertThrows(IllegalArgumentException.class, () -> solicitation.confirmDetails(
                Priority.LOW, TODAY, new BigDecimal("-0.01"), true, TODAY, NOW));
    }

    @Test
    void shouldRequireMinimumValueForHighPriority() {
        Solicitation solicitation = draftWithBasicDataAndAddress();
        assertThrows(IllegalArgumentException.class, () -> solicitation.confirmDetails(
                Priority.HIGH, TODAY, new BigDecimal("99.99"), true, TODAY, NOW));
        solicitation.confirmDetails(Priority.HIGH, TODAY, new BigDecimal("100.00"), true, TODAY, NOW);
        assertThat(solicitation.getEstimatedValue()).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldRequireTermsAcceptance() {
        Solicitation solicitation = draftWithBasicDataAndAddress();
        assertThrows(IllegalArgumentException.class, () -> solicitation.confirmDetails(
                Priority.LOW, TODAY, BigDecimal.ZERO, false, TODAY, NOW));
    }

    @Test
    void shouldRejectSubmissionWhenStepsAreIncomplete() {
        assertThrows(BusinessRuleException.class, () -> draft().submit(NOW));
        Solicitation missingAddress = draft();
        missingAddress.updateBasicData(ServiceType.INSTALLATION, "Instalação",
                "Solicito a instalação de um equipamento.", NOW);
        assertThrows(BusinessRuleException.class, () -> missingAddress.submit(NOW));
    }

    @Test
    void shouldSubmitCompleteRequestAndSetSubmittedTimestamp() {
        Solicitation solicitation = completeDraft();
        Instant submittedAt = NOW.plusSeconds(60);
        solicitation.submit(submittedAt);

        assertThat(solicitation.getStatus()).isEqualTo(SolicitationStatus.SUBMITTED);
        assertThat(solicitation.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(solicitation.getUpdatedAt()).isEqualTo(submittedAt);
    }

    @Test
    void shouldOnlyStartReviewForSubmittedRequest() {
        Solicitation draft = draft();
        assertThrows(IllegalStateException.class, () -> draft.startReview(UUID.randomUUID(), NOW));

        Solicitation submitted = completeDraft();
        submitted.submit(NOW);
        UUID analystId = UUID.randomUUID();
        submitted.startReview(analystId, NOW.plusSeconds(1));
        assertThat(submitted.getStatus()).isEqualTo(SolicitationStatus.IN_REVIEW);
        assertThat(submitted.getAnalystId()).isEqualTo(analystId);
    }

    @Test
    void shouldOnlyAllowAssignedAnalystToDecide() {
        UUID assignedAnalyst = UUID.randomUUID();
        Solicitation solicitation = inReview(assignedAnalyst);
        Decision decision = new Decision(assignedAnalyst, DecisionType.APPROVE,
                "A solicitação atende aos critérios.", NOW.plusSeconds(2));
        solicitation.decide(decision, NOW.plusSeconds(2));
        assertThat(solicitation.getStatus()).isEqualTo(SolicitationStatus.APPROVED);

        Solicitation anotherRequest = inReview(assignedAnalyst);
        Decision wrongAnalystDecision = new Decision(UUID.randomUUID(), DecisionType.REJECT,
                "A solicitação não atende aos critérios.", NOW.plusSeconds(2));
        assertThrows(IllegalArgumentException.class,
                () -> anotherRequest.decide(wrongAnalystDecision, NOW.plusSeconds(2)));
    }

    @Test
    void administratorCanDecideRequestAssignedToAnotherAnalyst() {
        Solicitation solicitation = inReview(UUID.randomUUID());
        Decision adminDecision = new Decision(UUID.randomUUID(), DecisionType.REJECT,
                "Rejeitada após revisão administrativa.", NOW.plusSeconds(2));
        solicitation.decideByAdministrator(adminDecision, NOW.plusSeconds(2));
        assertThat(solicitation.getStatus()).isEqualTo(SolicitationStatus.REJECTED);
        assertThat(solicitation.getDecision()).isEqualTo(adminDecision);
    }

    @Test
    void shouldRejectDecisionWhenRequestIsNotInReview() {
        Decision decision = new Decision(UUID.randomUUID(), DecisionType.APPROVE,
                "A solicitação atende aos critérios.", NOW);
        assertThrows(IllegalStateException.class, () -> draft().decide(decision, NOW));
    }

    @Test
    void decisionCommentMustContainBetweenTenAndOneThousandCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new Decision(UUID.randomUUID(),
                DecisionType.REJECT, "curto", NOW));
        assertThrows(IllegalArgumentException.class, () -> new Decision(UUID.randomUUID(),
                DecisionType.REJECT, "C".repeat(1001), NOW));
        assertThat(new Decision(UUID.randomUUID(), DecisionType.APPROVE,
                "Comentário válido", NOW).comment()).isEqualTo("Comentário válido");
    }

    private static Solicitation draft() {
        return new Solicitation(UUID.randomUUID(), UUID.randomUUID(), NOW);
    }

    private static Address address() {
        return new Address("70040010", "Esplanada dos Ministérios", "Brasília",
                "Zona Cívico-Administrativa", "DF", "10", null, true);
    }

    private static Solicitation draftWithBasicDataAndAddress() {
        Solicitation solicitation = draft();
        solicitation.updateBasicData(ServiceType.INSTALLATION, "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado.", NOW);
        solicitation.updateAddress(address(), NOW);
        return solicitation;
    }

    private static Solicitation completeDraft() {
        Solicitation solicitation = draftWithBasicDataAndAddress();
        solicitation.confirmDetails(Priority.MEDIUM, TODAY.plusDays(1),
                new BigDecimal("150.00"), true, TODAY, NOW);
        return solicitation;
    }

    private static Solicitation inReview(UUID analystId) {
        Solicitation solicitation = completeDraft();
        solicitation.submit(NOW);
        solicitation.startReview(analystId, NOW.plusSeconds(1));
        return solicitation;
    }
}
