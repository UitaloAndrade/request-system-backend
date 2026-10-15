package com.uitalo.desafiobackend.requestsystem.application.service.solicitation;

import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.model.SolicitationStatus;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSolicitationServiceTest {

    @Mock
    private SolicitationRepository solicitationRepository;

    @InjectMocks
    private CreateSolicitationService createSolicitationService;

    @Test
    void shouldCreateDraftSolicitationForClient() {
        UUID clientId = UUID.randomUUID();

        when(solicitationRepository.save(any(Solicitation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Solicitation result = createSolicitationService.create(clientId);

        ArgumentCaptor<Solicitation> captor =
                ArgumentCaptor.forClass(Solicitation.class);

        verify(solicitationRepository).save(captor.capture());

        Solicitation savedSolicitation = captor.getValue();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getClientId()).isEqualTo(clientId);
        assertThat(result.getStatus()).isEqualTo(SolicitationStatus.DRAFT);
        assertThat(result.getCurrentStep()).isEqualTo(1);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        assertThat(savedSolicitation.getId()).isEqualTo(result.getId());
        assertThat(savedSolicitation.getClientId()).isEqualTo(clientId);
    }

    @Test
    void shouldRejectCreationWithoutClientId() {
        assertThrows(
                NullPointerException.class,
                () -> createSolicitationService.create(null)
        );

        verifyNoInteractions(solicitationRepository);
    }
}
