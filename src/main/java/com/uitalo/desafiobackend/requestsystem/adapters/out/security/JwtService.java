package com.uitalo.desafiobackend.requestsystem.adapters.out.security;

import com.uitalo.desafiobackend.requestsystem.application.port.out.TokenProvider;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService implements TokenProvider {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:3600}")
            long expirationSeconds) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusSeconds(expirationSeconds)
                ))
                .signWith(signingKey)
                .compact();
    }

    public UUID extractUserId(String token) {
        String subject = extractClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    public boolean isValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    @Override
    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
