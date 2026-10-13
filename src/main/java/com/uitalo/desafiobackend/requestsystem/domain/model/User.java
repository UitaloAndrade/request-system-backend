package com.uitalo.desafiobackend.requestsystem.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final UserRole role;
    private final boolean active;
    private final Instant createdAt;

    public User(
            UUID id,
            String name,
            String email,
            String passwordHash,
            UserRole role,
            boolean active,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "Nome");
        this.email = requireText(email, "E-mail").toLowerCase();
        this.passwordHash = requireText(passwordHash, "Hash da senha");
        this.role = Objects.requireNonNull(role);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " é obrigatório.");
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
