package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.repository;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.jpa.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository
        extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}