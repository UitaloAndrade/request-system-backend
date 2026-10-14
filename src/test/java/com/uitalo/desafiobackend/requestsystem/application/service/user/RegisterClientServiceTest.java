package com.uitalo.desafiobackend.requestsystem.application.service.user;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PasswordHasher;
import com.uitalo.desafiobackend.requestsystem.application.service.user.RegisterClientService;
import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterClientServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private RegisterClientService service;

    @BeforeEach
    void setUp() {
        service = new RegisterClientService(
                userRepository,
                passwordHasher
        );
    }

    @Test
    void shouldRegisterClientSuccessfully() {
        when(userRepository.existsByEmail("cliente@example.com"))
                .thenReturn(false);

        when(passwordHasher.hash("SenhaSegura123"))
                .thenReturn("hash-da-senha");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.register(
                "Maria da Silva",
                "cliente@example.com",
                "SenhaSegura123"
        );

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Maria da Silva");
        assertThat(result.getEmail()).isEqualTo("cliente@example.com");
        assertThat(result.getPasswordHash()).isEqualTo("hash-da-senha");
        assertThat(result.getPasswordHash())
                .isNotEqualTo("SenhaSegura123");
        assertThat(result.getRole()).isEqualTo(UserRole.CLIENT);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();

        verify(userRepository).existsByEmail("cliente@example.com");
        verify(passwordHasher).hash("SenhaSegura123");
        verify(userRepository).save(result);
    }

    @Test
    void shouldNormalizeNameAndEmailBeforeSaving() {
        when(userRepository.existsByEmail("cliente@example.com"))
                .thenReturn(false);

        when(passwordHasher.hash("SenhaSegura123"))
                .thenReturn("hash-da-senha");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.register(
                "  Maria da Silva  ",
                "  CLIENTE@EXAMPLE.COM ",
                "SenhaSegura123"
        );

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertThat(savedUser.getName()).isEqualTo("Maria da Silva");
        assertThat(savedUser.getEmail()).isEqualTo("cliente@example.com");
        verify(userRepository).existsByEmail("cliente@example.com");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("cliente@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.register(
                "Maria da Silva",
                "cliente@example.com",
                "SenhaSegura123"
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Já existe um usuário");

        verify(userRepository, never()).save(any(User.class));
        verify(passwordHasher, never()).hash(anyString());
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> service.register(
                "   ",
                "cliente@example.com",
                "SenhaSegura123"
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("nome é obrigatório");

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldRejectInvalidEmail() {
        assertThatThrownBy(() -> service.register(
                "Maria da Silva",
                "email-invalido",
                "SenhaSegura123"
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("e-mail válido");

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldRejectPasswordWithLessThanEightCharacters() {
        assertThatThrownBy(() -> service.register(
                "Maria da Silva",
                "cliente@example.com",
                "1234567"
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("8 caracteres");

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldRejectNullPassword() {
        assertThatThrownBy(() -> service.register(
                "Maria da Silva",
                "cliente@example.com",
                null
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("8 caracteres");

        verifyNoInteractions(userRepository, passwordHasher);
    }

    @Test
    void shouldRejectNameLongerThan150Characters() {
        String longName = "A".repeat(151);

        assertThatThrownBy(() -> service.register(
                longName,
                "cliente@example.com",
                "SenhaSegura123"
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("150 caracteres");

        verifyNoInteractions(userRepository, passwordHasher);
    }
}
