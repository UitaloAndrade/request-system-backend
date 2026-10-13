package com.uitalo.desafiobackend.requestsystem.domain.repository;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    User save(User user);

    boolean existsByEmail(String email);
}
