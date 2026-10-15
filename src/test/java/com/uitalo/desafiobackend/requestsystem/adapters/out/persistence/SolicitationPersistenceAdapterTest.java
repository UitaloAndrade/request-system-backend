package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence;

import com.uitalo.desafiobackend.requestsystem.domain.model.*;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
class SolicitationPersistenceAdapterTest {

    @Autowired
    private SolicitationRepository solicitationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndRetrieveDraftSolicitation() {

        User savedUser = userRepository.save(
                createUser("cliente." + UUID.randomUUID() + "@example.com")
        );
        UUID clientId = savedUser.getId();

        Instant createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);

        Solicitation solicitation =
                new Solicitation(UUID.randomUUID(), clientId, createdAt);

        Solicitation saved = solicitationRepository.save(solicitation);

        assertThat(saved.getId()).isEqualTo(solicitation.getId());
        assertThat(saved.getClientId()).isEqualTo(clientId);
        assertThat(saved.getStatus()).isEqualTo(SolicitationStatus.DRAFT);
        assertThat(saved.getCurrentStep()).isEqualTo(1);

        Solicitation found = solicitationRepository
                .findById(solicitation.getId())
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(solicitation.getId());
        assertThat(found.getClientId()).isEqualTo(clientId);
        assertThat(found.getCreatedAt()).isEqualTo(createdAt);
        assertThat(found.getStatus()).isEqualTo(SolicitationStatus.DRAFT);
        assertThat(found.getCurrentStep()).isEqualTo(1);
    }

    @Test
    void shouldFindSolicitationByIdAndClientId() {
        User savedUser = userRepository.save(
                createUser("cliente." + UUID.randomUUID() + "@example.com")
        );
        UUID clientId = savedUser.getId();

        Solicitation solicitation = new Solicitation(
                UUID.randomUUID(),
                clientId,
                Instant.now()
        );

        solicitationRepository.save(solicitation);

        assertThat(
                solicitationRepository.findByIdAndClientId(
                        solicitation.getId(), clientId
                )
        ).isPresent();

        assertThat(
                solicitationRepository.findByIdAndClientId(
                        solicitation.getId(), UUID.randomUUID()
                )
        ).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenSolicitationDoesNotExist() {
        assertThat(solicitationRepository.findById(UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    void shouldUpdateExistingSolicitationWithoutCreatingAnotherRecord() {
        UUID solicitationId = UUID.randomUUID();

        User savedUser = userRepository.save(
                createUser("cliente." + UUID.randomUUID() + "@example.com")
        );
        UUID clientId = savedUser.getId();

        Solicitation solicitation = new Solicitation(
                solicitationId,
                clientId,
                Instant.now()
        );

        solicitationRepository.save(solicitation);

        solicitation.updateBasicData(
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicitação de instalação de equipamento no endereço informado.",
                Instant.now()
        );

        solicitationRepository.save(solicitation);

        Solicitation found = solicitationRepository
                .findById(solicitationId)
                .orElseThrow();

        assertThat(found.getId()).isEqualTo(solicitationId);
        assertThat(found.getServiceType()).isEqualTo(ServiceType.INSTALLATION);
        assertThat(found.getTitle()).isEqualTo("Instalação de equipamento");
        assertThat(found.getDescription())
                .isEqualTo("Solicitação de instalação de equipamento no endereço informado.");
        assertThat(found.getStatus()).isEqualTo(SolicitationStatus.DRAFT);
    }

    @Test
    void shouldPersistCompleteSolicitationAndDecision() {
        User savedUser = userRepository.save(
                createUser("cliente." + UUID.randomUUID() + "@example.com")
        );
        UUID clientId = savedUser.getId();

        User analyst = userRepository.save(
                createUser("analyst." + UUID.randomUUID() + "@example.com")
        );
        UUID analystId =  analyst.getId();

        Instant createdAt = Instant.now();

        Solicitation solicitation = new Solicitation(
                UUID.randomUUID(),
                clientId,
                createdAt
        );

        solicitation.updateBasicData(
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicito a instalação de equipamento no endereço informado.",
                Instant.now()
        );

        Address address = new Address(
                "70040010",
                "Esplanada dos Ministérios",
                "Brasília",
                "Zona Cívico-Administrativa",
                "DF",
                "12A",
                "Bloco A",
                true
        );

        solicitation.updateAddress(address, Instant.now());

        solicitation.confirmDetails(
                Priority.HIGH,
                LocalDate.now().plusDays(10),
                new BigDecimal("150.00"),
                true,
                LocalDate.now(),
                Instant.now()
        );

        solicitation.submit(Instant.now());
        solicitation.startReview(analystId, Instant.now());

        Decision decision = new Decision(
                analystId,
                DecisionType.APPROVE,
                "Documentação e condições da solicitação foram verificadas.",
                Instant.now()
        );

        solicitation.decide(decision, Instant.now());

        solicitationRepository.save(solicitation);

        Solicitation found = solicitationRepository
                .findById(solicitation.getId())
                .orElseThrow();

        assertThat(found.getClientId()).isEqualTo(clientId);
        assertThat(found.getServiceType()).isEqualTo(ServiceType.INSTALLATION);
        assertThat(found.getTitle()).isEqualTo("Instalação de equipamento");
        assertThat(found.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(found.getPreferredDate())
                .isEqualTo(LocalDate.now().plusDays(10));
        assertThat(found.getEstimatedValue())
                .isEqualByComparingTo("150.00");
        assertThat(found.getTermsAccepted()).isTrue();

        assertThat(found.getAddress()).isNotNull();
        assertThat(found.getAddress().cep()).isEqualTo("70040010");
        assertThat(found.getAddress().street())
                .isEqualTo("Esplanada dos Ministérios");
        assertThat(found.getAddress().state()).isEqualTo("DF");
        assertThat(found.getAddress().number()).isEqualTo("12A");
        assertThat(found.getAddress().complement()).isEqualTo("Bloco A");
        assertThat(found.getAddress().postalCodeValidated()).isTrue();

        assertThat(found.getStatus()).isEqualTo(SolicitationStatus.APPROVED);
        assertThat(found.getAnalystId()).isEqualTo(analystId);
        assertThat(found.getDecision()).isNotNull();
        assertThat(found.getDecision().analystId()).isEqualTo(analystId);
        assertThat(found.getDecision().type()).isEqualTo(DecisionType.APPROVE);
        assertThat(found.getDecision().comment())
                .isEqualTo("Documentação e condições da solicitação foram verificadas.");
        assertThat(found.getSubmittedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    private User createUser(String email) {
        return new User(
                UUID.randomUUID(),
                "Usuário de Teste",
                email,
                "hash-de-senha-de-teste",
                UserRole.CLIENT,
                true,
                Instant.now()
        );
    }
}