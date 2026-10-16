package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PostalCodeLookup;
import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.LookupAddressUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.*;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitationUpdateUseCasesTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @Mock
    private LookupAddressUseCase lookupAddressUseCase;

    private UpdateSolicitationBasicDataService basicDataService;
    private UpdateSolicitationAddressService addressService;
    private UpdateSolicitationDetailsService detailsService;
    private SubmitSolicitationService submitService;

    private UUID solicitationId;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        basicDataService = new UpdateSolicitationBasicDataService(
                solicitationRepository);

        addressService = new UpdateSolicitationAddressService(
                solicitationRepository,
                lookupAddressUseCase);

        detailsService = new UpdateSolicitationDetailsService(
                solicitationRepository);

        submitService = new SubmitSolicitationService(
                solicitationRepository);

        solicitationId = UUID.randomUUID();
        clientId = UUID.randomUUID();
    }

    @Test
    void shouldUpdateBasicData() {
        Solicitation solicitation = newDraftSolicitation();

        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.of(solicitation));

        mockSave();

        Solicitation result = basicDataService.update(
                solicitationId,
                clientId,
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado."
        );

        assertThat(result.getServiceType())
                .isEqualTo(ServiceType.INSTALLATION);
        assertThat(result.getTitle())
                .isEqualTo("Instalação de equipamento");
        assertThat(result.getDescription())
                .isEqualTo("Solicito a instalação de um equipamento no endereço informado.");
        assertThat(result.getCurrentStep()).isGreaterThanOrEqualTo(2);

        verify(solicitationRepository).save(solicitation);
    }

    @Test
    void shouldAllowAdministratorToUpdateSolicitationNotOwnedByCurrentUser() {
        Solicitation solicitation = newDraftSolicitation();
        when(solicitationRepository.findById(solicitationId))
                .thenReturn(Optional.of(solicitation));
        mockSave();

        Solicitation result = basicDataService.update(
                solicitationId,
                clientId,
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado.",
                true
        );

        assertThat(result.getTitle()).isEqualTo("Instalação de equipamento");
        verify(solicitationRepository).findById(solicitationId);
        verify(solicitationRepository, never()).findByIdAndClientId(any(), any());
        verify(solicitationRepository).save(solicitation);
    }

    @Test
    void shouldNotUpdateBasicDataWhenSolicitationDoesNotBelongToClient() {
        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.empty());

        assertThrows(
                SolicitationNotFoundException.class,
                () -> basicDataService.update(
                        solicitationId,
                        clientId,
                        ServiceType.INSTALLATION,
                        "Instalação de equipamento",
                        "Solicito a instalação de um equipamento no endereço informado."
                )
        );

        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void shouldUpdateAddressUsingPostalCodeLookup() {
        Solicitation solicitation = newDraftSolicitation();

        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.of(solicitation));

        when(lookupAddressUseCase.lookup("70040-010"))
                .thenReturn(new PostalCodeLookup.PostalCodeAddress(
                        "70040010",
                        "Esplanada dos Ministérios",
                        "Zona Cívico-Administrativa",
                        "Brasília",
                        "DF"
                ));

        mockSave();

        Solicitation result = addressService.update(
                solicitationId,
                clientId,
                "70040-010",
                "12A",
                "Bloco A"
        );

        assertThat(result.getAddress()).isNotNull();
        assertThat(result.getAddress().cep()).isEqualTo("70040010");
        assertThat(result.getAddress().street())
                .isEqualTo("Esplanada dos Ministérios");
        assertThat(result.getAddress().city()).isEqualTo("Brasília");
        assertThat(result.getAddress().state()).isEqualTo("DF");
        assertThat(result.getAddress().number()).isEqualTo("12A");
        assertThat(result.getAddress().complement()).isEqualTo("Bloco A");
        assertThat(result.getAddress().postalCodeValidated()).isTrue();

        verify(lookupAddressUseCase).lookup("70040-010");
        verify(solicitationRepository).save(solicitation);
    }

    @Test
    void shouldNotSaveAddressWhenPostalCodeLookupFails() {
        Solicitation solicitation = newDraftSolicitation();

        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.of(solicitation));

        when(lookupAddressUseCase.lookup("00000000"))
                .thenThrow(new IllegalStateException("Falha na consulta do CEP"));

        assertThrows(
                IllegalStateException.class,
                () -> addressService.update(
                        solicitationId,
                        clientId,
                        "00000000",
                        "10",
                        null
                )
        );

        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateAddressWhenSolicitationDoesNotExist() {
        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.empty());

        assertThrows(
                SolicitationNotFoundException.class,
                () -> addressService.update(
                        solicitationId,
                        clientId,
                        "70040010",
                        "10",
                        null
                )
        );

        verifyNoInteractions(lookupAddressUseCase);
        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void shouldUpdateDetails() {
        Solicitation solicitation = newDraftSolicitation();
        LocalDate preferredDate = LocalDate.now().plusDays(10);

        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.of(solicitation));

        mockSave();

        Solicitation result = detailsService.update(
                solicitationId,
                clientId,
                Priority.MEDIUM,
                preferredDate,
                new BigDecimal("150.00"),
                true
        );

        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(result.getPreferredDate()).isEqualTo(preferredDate);
        assertThat(result.getEstimatedValue())
                .isEqualByComparingTo("150.00");
        assertThat(result.getTermsAccepted()).isTrue();

        verify(solicitationRepository).save(solicitation);
    }

    @Test
    void shouldNotUpdateDetailsWhenSolicitationDoesNotExist() {
        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.empty());

        assertThrows(
                SolicitationNotFoundException.class,
                () -> detailsService.update(
                        solicitationId,
                        clientId,
                        Priority.MEDIUM,
                        LocalDate.now().plusDays(10),
                        new BigDecimal("150.00"),
                        true
                )
        );

        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void shouldSubmitCompleteSolicitation() {
        Solicitation solicitation = newCompleteDraftSolicitation();

        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.of(solicitation));

        mockSave();

        Solicitation result = submitService.submit(
                solicitationId, clientId);

        assertThat(result.getStatus())
                .isEqualTo(SolicitationStatus.SUBMITTED);
        assertThat(result.getSubmittedAt()).isNotNull();

        verify(solicitationRepository).save(solicitation);
    }

    @Test
    void shouldNotSubmitIncompleteSolicitation() {
        Solicitation solicitation = newDraftSolicitation();

        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.of(solicitation));

        assertThrows(
                IllegalStateException.class,
                () -> submitService.submit(solicitationId, clientId)
        );

        verify(solicitationRepository, never()).save(any());
    }

    @Test
    void shouldNotSubmitWhenSolicitationDoesNotExist() {
        when(solicitationRepository.findByIdAndClientId(
                solicitationId, clientId))
                .thenReturn(Optional.empty());

        assertThrows(
                SolicitationNotFoundException.class,
                () -> submitService.submit(solicitationId, clientId)
        );

        verify(solicitationRepository, never()).save(any());
    }

    private Solicitation newDraftSolicitation() {
        Instant now = Instant.now();
        return new Solicitation(
                solicitationId,
                clientId,
                now
        );
    }

    private Solicitation newCompleteDraftSolicitation() {
        Solicitation solicitation = newDraftSolicitation();

        solicitation.updateBasicData(
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado.",
                Instant.now()
        );

        solicitation.updateAddress(
                new Address(
                        "70040010",
                        "Esplanada dos Ministérios",
                        "Brasília",
                        "Zona Cívico-Administrativa",
                        "DF",
                        "12A",
                        "Bloco A",
                        true
                ),
                Instant.now()
        );

        solicitation.confirmDetails(
                Priority.MEDIUM,
                LocalDate.now().plusDays(10),
                new BigDecimal("150.00"),
                true,
                LocalDate.now(),
                Instant.now()
        );

        return solicitation;
    }

    private void mockSave() {
        when(solicitationRepository.save(any(Solicitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
