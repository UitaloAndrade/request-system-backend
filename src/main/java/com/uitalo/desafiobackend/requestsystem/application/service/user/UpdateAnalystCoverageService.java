package com.uitalo.desafiobackend.requestsystem.application.service.user;

import com.uitalo.desafiobackend.requestsystem.application.usecase.user.UpdateAnalystCoverageUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UpdateAnalystCoverageService
        implements UpdateAnalystCoverageUseCase {

    private static final Set<String> VALID_STATES = Set.of(
            "AC", "AL", "AP", "AM", "BA",
            "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB",
            "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP",
            "SE", "TO"
    );

    private final UserRepository userRepository;
    private final AnalystCoverageRepository coverageRepository;

    public UpdateAnalystCoverageService(
            UserRepository userRepository,
            AnalystCoverageRepository coverageRepository
    ) {
        this.userRepository = userRepository;
        this.coverageRepository = coverageRepository;
    }

    @Override
    @Transactional
    public void updateCoverage(UUID analystId, List<String> states) {
        if (analystId == null) {
            throw new BusinessRuleException(
                    "O identificador do analista é obrigatório."
            );
        }

        if (states == null || states.isEmpty()) {
            throw new BusinessRuleException(
                    "Informe pelo menos uma UF de cobertura."
            );
        }

        User analyst = userRepository.findById(analystId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Usuário não encontrado."
                ));

        if (analyst.getRole() != UserRole.ANALYST) {
            throw new BusinessRuleException(
                    "Somente usuários com papel ANALYST podem ter UFs de cobertura."
            );
        }

        List<String> normalizedStates = states.stream()
                .map(this::normalizeState)
                .distinct()
                .toList();

        coverageRepository.deleteByAnalystId(analystId);

        List<AnalystCoverage> coverages = normalizedStates.stream()
                .map(state -> new AnalystCoverage(analystId, state))
                .toList();

        coverageRepository.saveAll(coverages);
    }

    private String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            throw new BusinessRuleException(
                    "A UF de cobertura não pode ser vazia."
            );
        }

        String normalized = state.trim().toUpperCase(Locale.ROOT);

        if (!VALID_STATES.contains(normalized)) {
            throw new BusinessRuleException(
                    "UF inválida: " + normalized
            );
        }

        return normalized;
    }
}
