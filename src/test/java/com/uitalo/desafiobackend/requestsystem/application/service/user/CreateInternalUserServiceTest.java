package com.uitalo.desafiobackend.requestsystem.application.service.user;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PasswordHasher;
import com.uitalo.desafiobackend.requestsystem.application.service.user.CreateInternalUserService;
import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateInternalUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private CreateInternalUserService service;

    @BeforeEach
    void setUp() {
        service = new CreateInternalUserService(
                userRepository,
                passwordHasher
        );
    }

    @Test
    void shouldCreateAnalystSuccessfully() {
        when(userRepository.existsByEmail("analista@example.com"))
                .thenReturn(false);

        when(passwordHasher.hash("SenhaSegura123"))
                .thenReturn("hash-da-senha");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.create(
                "João Analista",
                "analista@example.com",
                "SenhaSegura123",
                UserRole.ANALYST
        );

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("João Analista");
        assertThat(result.getEmail()).isEqualTo("analista@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.ANALYST);
        assertThat(result.getPasswordHash()).isEqualTo("hash-da-senha");
        assertThat(result.isActive()).isTrue();

        verify(userRepository).save(result);
    }

    @Test
    void shouldCreateAdminSuccessfully() {
        when(userRepository.existsByEmail("admin@example.com"))
                .thenReturn(false);

        when(passwordHasher.hash("SenhaSegura123"))
                .thenReturn("hash-da-senha");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.create(
                "Administrador",
                "admin@example.com",
                "SenhaSegura123",
                UserRole.ADMIN
        );

        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(result.getEmail()).isEqualTo("admin@example.com");

        verify(userRepository).save(result);
    }

    @Test
    void shouldNormalizeNameAndEmail() {
        when(userRepository.existsByEmail("analista@example.com"))
                .thenReturn(false);

        when(passwordHasher.hash("SenhaSegura123"))
                .thenReturn("hash-da-senha");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.create(
                "  João Analista  ",
                "  ANALISTA@EXAMPLE.COM ",
                "SenhaSegura123",
                UserRole.ANALYST
        );

        assertThat(result.getName()).isEqualTo("João Analista");
        assertThat(result.getEmail()).isEqualTo("analista@example.com");
    }

    @Test
    void shouldRejectClientRole() {
        assertThatThrownBy(() -> service.create(
                "Cliente",
                "cliente@example.com",
                "SenhaSegura123",
                UserRole.CLIENT
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ANALYST ou ADMIN");

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldRejectNullRole() {
        assertThatThrownBy(() -> service.create(
                "Usuário Interno",
                "interno@example.com",
                "SenhaSegura123",
                null
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ANALYST ou ADMIN");

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("analista@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(
                "João Analista",
                "analista@example.com",
                "SenhaSegura123",
                UserRole.ANALYST
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Já existe um usuário");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordHasher, never()).hash(anyString());
    }

    @Test
    void shouldRejectInvalidPassword() {
        assertThatThrownBy(() -> service.create(
                "João Analista",
                "analista@example.com",
                "123",
                UserRole.ANALYST
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("8 caracteres");

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> service.create(
                "João Analista",
                "email-invalido",
                "SenhaSegura123",
                UserRole.ANALYST
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("e-mail válido");

        verifyNoInteractions(userRepository, passwordHasher);
    }
}