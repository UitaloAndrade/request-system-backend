package com.uitalo.desafiobackend.requestsystem.adapters.out.security;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockFilterChain;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtService, userRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarUsuarioComTokenValido()
            throws ServletException, IOException {

        UUID userId = UUID.randomUUID();
        User user = createUser(userId, UserRole.CLIENT, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUserId("token-valido")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        assertNotNull(authentication);
        assertEquals(userId.toString(), authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority()
                        .equals("ROLE_CLIENT")));
        assertEquals(200, response.getStatus());
        assertNotNull(chain.getRequest());

        verify(userRepository).findById(userId);
    }

    @Test
    void deveRejeitarTokenInvalido()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-invalido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUserId("token-invalido"))
                .thenThrow(new IllegalArgumentException("Token inválido"));

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(chain.getRequest());

        verifyNoInteractions(userRepository);
    }

    @Test
    void deveRejeitarUsuarioQueNaoExisteMais()
            throws ServletException, IOException {

        UUID userId = UUID.randomUUID();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUserId("token-valido")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(chain.getRequest());
    }

    @Test
    void deveRejeitarUsuarioInativoMesmoComTokenValido()
            throws ServletException, IOException {

        UUID userId = UUID.randomUUID();
        User user = createUser(userId, UserRole.ADMIN, false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUserId("token-valido")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(chain.getRequest());
    }

    @Test
    void deveContinuarCadeiaQuandoTokenNaoForEnviado()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(chain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void deveUtilizarPapelAtualDoBancoEnaoPapelInformadoNoToken()
            throws ServletException, IOException {

        UUID userId = UUID.randomUUID();
        User user = createUser(userId, UserRole.ANALYST, true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-valido");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        when(jwtService.extractUserId("token-valido")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority()
                        .equals("ROLE_ANALYST")));
        assertFalse(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority()
                        .equals("ROLE_ADMIN")));
    }

    private User createUser(UUID id, UserRole role, boolean active) {
        return new User(
                id,
                "Usuário de Teste",
                "teste@example.com",
                "hash-bcrypt",
                role,
                active,
                Instant.now()
        );
    }
}
