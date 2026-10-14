package com.uitalo.desafiobackend.requestsystem.application.service.user;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PasswordHasher;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.CreateInternalUserUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class CreateInternalUserService
        implements CreateInternalUserUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CreateInternalUserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    @Transactional
    public User create(
            String name,
            String email,
            String rawPassword,
            UserRole role
    ) {
        String normalizedName = normalizeName(name);
        String normalizedEmail = normalizeEmail(email);

        validatePassword(rawPassword);
        validateInternalRole(role);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException(
                    "Já existe um usuário cadastrado com este e-mail."
            );
        }

        User user = new User(
                UUID.randomUUID(),
                normalizedName,
                normalizedEmail,
                passwordHasher.hash(rawPassword),
                role,
                true,
                Instant.now()
        );

        return userRepository.save(user);
    }

    private void validateInternalRole(UserRole role) {
        if (role != UserRole.ANALYST && role != UserRole.ADMIN) {
            throw new BusinessRuleException(
                    "Usuários internos devem possuir papel ANALYST ou ADMIN."
            );
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleException(
                    "O nome é obrigatório."
            );
        }

        String normalized = name.trim();

        if (normalized.length() > 150) {
            throw new BusinessRuleException(
                    "O nome deve ter no máximo 150 caracteres."
            );
        }

        return normalized;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleException(
                    "O e-mail é obrigatório."
            );
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);

        if (normalized.length() > 254
                || !normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BusinessRuleException(
                    "Informe um e-mail válido."
            );
        }

        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessRuleException(
                    "A senha deve possuir pelo menos 8 caracteres."
            );
        }
    }
}