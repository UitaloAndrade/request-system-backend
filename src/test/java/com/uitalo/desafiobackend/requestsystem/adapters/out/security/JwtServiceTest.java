package com.uitalo.desafiobackend.requestsystem.adapters.out.security;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600);
    }

    @Test
    void deveGerarTokenParaUsuario() {
        User user = createUser(UserRole.CLIENT, true);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void deveExtrairIdDoUsuarioDoToken() {
        User user = createUser(UserRole.CLIENT, true);

        String token = jwtService.generateToken(user);

        assertEquals(user.getId(), jwtService.extractUserId(token));
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        User user = createUser(UserRole.CLIENT, true);
        String token = jwtService.generateToken(user);

        String tokenAdulterado = token.substring(0, token.length() - 2) + "xx";

        assertFalse(jwtService.isValid(tokenAdulterado));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        String tokenExpirado = Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertFalse(jwtService.isValid(tokenExpirado));
    }

    @Test
    void deveInformarTempoDeExpiracaoConfigurado() {
        assertEquals(3600, jwtService.getExpirationSeconds());
    }

    private User createUser(UserRole role, boolean active) {
        return new User(
                UUID.randomUUID(),
                "Usuário de Teste",
                "teste@example.com",
                "hash-bcrypt",
                role,
                active,
                Instant.now()
        );
    }
}
