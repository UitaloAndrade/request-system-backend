package com.uitalo.desafiobackend.requestsystem.application.service.user;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PasswordHasher;
import com.uitalo.desafiobackend.requestsystem.application.port.out.TokenProvider;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.dto.LoginResult;
import com.uitalo.desafiobackend.requestsystem.domain.execption.InvalidCredentialsException;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(
                userRepository,
                passwordHasher,
                tokenProvider
        );
    }

    @Test
    void deveAutenticarUsuarioEEmitirToken() {
        User user = createUser(true);

        when(userRepository.findByEmail("teste@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("SenhaSegura123", "hash-bcrypt"))
                .thenReturn(true);
        when(tokenProvider.generateToken(user))
                .thenReturn("jwt-de-teste");
        when(tokenProvider.getExpirationSeconds())
                .thenReturn(3600L);

        LoginResult result = loginService.login(
                "teste@example.com",
                "SenhaSegura123"
        );

        assertEquals(user, result.user());
        assertEquals("jwt-de-teste", result.accessToken());
        assertEquals(3600L, result.expiresIn());

        verify(passwordHasher).matches("SenhaSegura123", "hash-bcrypt");
        verify(tokenProvider).generateToken(user);
    }

    @Test
    void deveNormalizarEmailAntesDeConsultarRepositorio() {
        User user = createUser(true);

        when(userRepository.findByEmail("teste@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("SenhaSegura123", "hash-bcrypt"))
                .thenReturn(true);
        when(tokenProvider.generateToken(user))
                .thenReturn("jwt-de-teste");
        when(tokenProvider.getExpirationSeconds())
                .thenReturn(3600L);

        LoginResult result = loginService.login(
                "  TESTE@EXAMPLE.COM ",
                "SenhaSegura123"
        );

        assertEquals("jwt-de-teste", result.accessToken());

        verify(userRepository).findByEmail("teste@example.com");
    }

    @Test
    void deveRejeitarEmailInexistente() {
        when(userRepository.findByEmail("desconhecido@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        "desconhecido@example.com",
                        "SenhaSegura123"
                )
        );

        verifyNoInteractions(passwordHasher, tokenProvider);
    }

    @Test
    void deveRejeitarSenhaIncorreta() {
        User user = createUser(true);

        when(userRepository.findByEmail("teste@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordHasher.matches("senha-errada", "hash-bcrypt"))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        "teste@example.com",
                        "senha-errada"
                )
        );

        verifyNoInteractions(tokenProvider);
    }

    @Test
    void deveRejeitarUsuarioInativo() {
        User user = createUser(false);

        when(userRepository.findByEmail("teste@example.com"))
                .thenReturn(Optional.of(user));

        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login(
                        "teste@example.com",
                        "SenhaSegura123"
                )
        );

        verifyNoInteractions(passwordHasher, tokenProvider);
    }

    @Test
    void deveRejeitarEmailVazio() {
        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login("", "SenhaSegura123")
        );

        verifyNoInteractions(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    void deveRejeitarSenhaVazia() {
        assertThrows(
                InvalidCredentialsException.class,
                () -> loginService.login("teste@example.com", " ")
        );

        verifyNoInteractions(userRepository, passwordHasher, tokenProvider);
    }

    private User createUser(boolean active) {
        return new User(
                UUID.randomUUID(),
                "Usuário de Teste",
                "teste@example.com",
                "hash-bcrypt",
                UserRole.CLIENT,
                active,
                Instant.now()
        );
    }
}