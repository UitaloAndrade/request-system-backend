package com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.adapter;

import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity.UserJpaEntity;
import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.mapper.UserPersistenceMapper;
import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.repository.SpringDataUserRepository;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements UserRepository {

    private final SpringDataUserRepository jpaRepository;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(
            SpringDataUserRepository jpaRepository,
            UserPersistenceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return jpaRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserJpaEntity entity = mapper.toEntity(user);

        UserJpaEntity savedEntity = jpaRepository.save(entity);

        return mapper.toDomain(savedEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);

        return jpaRepository.existsByEmailIgnoreCase(normalizedEmail);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O e-mail é obrigatório para a consulta."
            );
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}