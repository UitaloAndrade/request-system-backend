package com.uitalo.desafiobackend.requestsystem.service.user;

import com.uitalo.desafiobackend.requestsystem.application.service.user.UpdateAnalystCoverageService;
import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAnalystCoverageServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnalystCoverageRepository coverageRepository;

    private UpdateAnalystCoverageService service;

    @BeforeEach
    void setUp() {
        service = new UpdateAnalystCoverageService(
                userRepository,
                coverageRepository
        );
    }

    @Test
    void shouldUpdateAnalystCoveragesSuccessfully() {
        UUID analystId = UUID.randomUUID();
        User analyst = createUser(analystId, UserRole.ANALYST);

        when(userRepository.findById(analystId))
                .thenReturn(Optional.of(analyst));

        when(coverageRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateCoverage(
                analystId,
                List.of("DF", "GO", "SP")
        );

        verify(coverageRepository).deleteByAnalystId(analystId);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AnalystCoverage>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(coverageRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(AnalystCoverage::state)
                .containsExactly("DF", "GO", "SP");
    }

    @Test
    void shouldNormalizeStatesAndRemoveDuplicates() {
        UUID analystId = UUID.randomUUID();
        User analyst = createUser(analystId, UserRole.ANALYST);

        when(userRepository.findById(analystId))
                .thenReturn(Optional.of(analyst));

        when(coverageRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateCoverage(
                analystId,
                List.of("df", "DF", " go ", "Go")
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AnalystCoverage>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(coverageRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(AnalystCoverage::state)
                .containsExactly("DF", "GO");

        verify(coverageRepository).deleteByAnalystId(analystId);
    }

    @Test
    void shouldRejectNullAnalystId() {
        assertThatThrownBy(() ->
                service.updateCoverage(null, List.of("DF"))
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("identificador");

        verifyNoInteractions(userRepository, coverageRepository);
    }

    @Test
    void shouldRejectEmptyStates() {
        UUID analystId = UUID.randomUUID();

        assertThatThrownBy(() ->
                service.updateCoverage(analystId, List.of())
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pelo menos uma UF");

        verifyNoInteractions(userRepository, coverageRepository);
    }

    @Test
    void shouldRejectNullStatesList() {
        UUID analystId = UUID.randomUUID();

        assertThatThrownBy(() ->
                service.updateCoverage(analystId, null)
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pelo menos uma UF");

        verifyNoInteractions(userRepository, coverageRepository);
    }

    @Test
    void shouldRejectUnknownUser() {
        UUID analystId = UUID.randomUUID();

        when(userRepository.findById(analystId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateCoverage(analystId, List.of("DF"))
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(coverageRepository, never())
                .deleteByAnalystId(any(UUID.class));

        verify(coverageRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldRejectUserWhoIsNotAnalyst() {
        UUID userId = UUID.randomUUID();
        User admin = createUser(userId, UserRole.ADMIN);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(admin));

        assertThatThrownBy(() ->
                service.updateCoverage(userId, List.of("DF"))
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Somente usuários");

        verify(coverageRepository, never())
                .deleteByAnalystId(any(UUID.class));

        verify(coverageRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldRejectInvalidState() {
        UUID analystId = UUID.randomUUID();
        User analyst = createUser(analystId, UserRole.ANALYST);

        when(userRepository.findById(analystId))
                .thenReturn(Optional.of(analyst));

        assertThatThrownBy(() ->
                service.updateCoverage(analystId, List.of("XX"))
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("UF inválida");

        // A validação deve ocorrer antes de apagar as coberturas atuais.
        verify(coverageRepository, never())
                .deleteByAnalystId(any(UUID.class));

        verify(coverageRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldRejectBlankState() {
        UUID analystId = UUID.randomUUID();
        User analyst = createUser(analystId, UserRole.ANALYST);

        when(userRepository.findById(analystId))
                .thenReturn(Optional.of(analyst));

        assertThatThrownBy(() ->
                service.updateCoverage(analystId, List.of("  "))
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não pode ser vazia");

        verify(coverageRepository, never())
                .deleteByAnalystId(any(UUID.class));

        verify(coverageRepository, never()).saveAll(anyList());
    }

    @Test
    void shouldRejectNullStateInsideList() {
        UUID analystId = UUID.randomUUID();
        User analyst = createUser(analystId, UserRole.ANALYST);

        when(userRepository.findById(analystId))
                .thenReturn(Optional.of(analyst));

        assertThatThrownBy(() ->
                service.updateCoverage(
                        analystId,
                        java.util.Arrays.asList("DF", null)
                )
        )
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("não pode ser vazia");

        verify(coverageRepository, never())
                .deleteByAnalystId(any(UUID.class));

        verify(coverageRepository, never()).saveAll(anyList());
    }

    private User createUser(UUID id, UserRole role) {
        return new User(
                id,
                "Usuário de Teste",
                "usuario-" + id + "@example.com",
                "hash-de-senha",
                role,
                true,
                Instant.now()
        );
    }
}