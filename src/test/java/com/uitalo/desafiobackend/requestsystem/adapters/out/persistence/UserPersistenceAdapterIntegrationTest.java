package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserPersistenceAdapterIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserById() {
        // Arrange
        User user = createUser("cliente@example.com");

        // Act
        User savedUser = userRepository.save(user);

        User foundUser = userRepository.findById(savedUser.getId())
                .orElseThrow();

        // Assert
        assertThat(foundUser.getId()).isEqualTo(user.getId());
        assertThat(foundUser.getName()).isEqualTo("Usuário de Teste");
        assertThat(foundUser.getEmail())
                .isEqualTo("cliente@example.com");
        assertThat(foundUser.getRole()).isEqualTo(UserRole.CLIENT);
    }

    @Test
    void shouldFindUserByEmailIgnoringCase() {
        // Arrange
        User user = createUser("analista@example.com");
        userRepository.save(user);

        // Act
        User foundUser = userRepository
                .findByEmail("ANALISTA@EXAMPLE.COM")
                .orElseThrow();

        // Assert
        assertThat(foundUser.getId()).isEqualTo(user.getId());
        assertThat(foundUser.getEmail())
                .isEqualTo("analista@example.com");
    }

    @Test
    void shouldCheckWhetherEmailExists() {
        // Arrange
        User user = createUser("admin@example.com");
        userRepository.save(user);

        // Act / Assert
        assertThat(userRepository.existsByEmail("admin@example.com"))
                .isTrue();

        assertThat(userRepository.existsByEmail("inexistente@example.com"))
                .isFalse();
    }

    private User createUser(String email) {
        return new User(
                UUID.randomUUID(),
                "Usuário de Teste",
                email,
                "hash-de-senha-de-teste",
                UserRole.CLIENT,
                true,
                Instant.now()
        );
    }
}