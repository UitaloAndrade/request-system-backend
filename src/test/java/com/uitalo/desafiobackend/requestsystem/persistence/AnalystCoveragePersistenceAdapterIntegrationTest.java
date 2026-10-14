package com.uitalo.desafiobackend.requestsystem.persistence;

import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AnalystCoveragePersistenceAdapterIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnalystCoverageRepository coverageRepository;

    @Test
    void shouldSaveAndFindAnalystCoverages() {
        User analyst = createAnalyst();
        userRepository.save(analyst);

        List<AnalystCoverage> coverages = List.of(
                new AnalystCoverage(analyst.getId(), "DF"),
                new AnalystCoverage(analyst.getId(), "GO"),
                new AnalystCoverage(analyst.getId(), "SP")
        );

        coverageRepository.saveAll(coverages);

        List<AnalystCoverage> foundCoverages =
                coverageRepository.findByAnalystId(analyst.getId());

        assertThat(foundCoverages)
                .hasSize(3)
                .extracting(AnalystCoverage::state)
                .containsExactlyInAnyOrder("DF", "GO", "SP");
    }

    @Test
    void shouldCheckWhetherAnalystCoversState() {
        User analyst = createAnalyst();
        userRepository.save(analyst);

        coverageRepository.saveAll(List.of(
                new AnalystCoverage(analyst.getId(), "DF"),
                new AnalystCoverage(analyst.getId(), "GO")
        ));

        assertThat(
                coverageRepository.existsByAnalystIdAndState(
                        analyst.getId(), "DF"
                )
        ).isTrue();

        assertThat(
                coverageRepository.existsByAnalystIdAndState(
                        analyst.getId(), "SP"
                )
        ).isFalse();
    }

    @Test
    void shouldDeleteAllCoveragesForAnalyst() {
        User analyst = createAnalyst();
        userRepository.save(analyst);

        coverageRepository.saveAll(List.of(
                new AnalystCoverage(analyst.getId(), "DF"),
                new AnalystCoverage(analyst.getId(), "GO")
        ));

        coverageRepository.deleteByAnalystId(analyst.getId());

        assertThat(
                coverageRepository.findByAnalystId(analyst.getId())
        ).isEmpty();
    }

    private User createAnalyst() {
        return new User(
                UUID.randomUUID(),
                "Analista de Teste",
                "analista@example.com",
                "hash-de-senha-de-teste",
                UserRole.ANALYST,
                true,
                Instant.now()
        );
    }
}
