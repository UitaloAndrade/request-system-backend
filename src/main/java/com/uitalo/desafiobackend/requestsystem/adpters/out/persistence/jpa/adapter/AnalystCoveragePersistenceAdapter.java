package com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.adapter;

import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity.AnalystCoverageJpaEntity;
import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.mapper.AnalystCoveragePersistenceMapper;
import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.repository.SpringDataAnalystCoverageRepository;
import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.repository.SpringDataUserRepository;
import com.uitalo.desafiobackend.requestsystem.domain.execption.UserNotFoundException;
import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AnalystCoverageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class AnalystCoveragePersistenceAdapter
        implements AnalystCoverageRepository {

    private final SpringDataAnalystCoverageRepository coverageRepository;
    private final SpringDataUserRepository userRepository;
    private final AnalystCoveragePersistenceMapper mapper;

    public AnalystCoveragePersistenceAdapter(
            SpringDataAnalystCoverageRepository coverageRepository,
            SpringDataUserRepository userRepository,
            AnalystCoveragePersistenceMapper mapper
    ) {
        this.coverageRepository = coverageRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public List<AnalystCoverage> findByAnalystId(UUID analystId) {
        return coverageRepository.findAllByIdAnalystId(analystId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByAnalystIdAndState(
            UUID analystId,
            String state
    ) {
        return coverageRepository.existsByIdAnalystIdAndIdState(
                analystId,
                state
        );
    }

    @Override
    @Transactional
    public List<AnalystCoverage> saveAll(
            List<AnalystCoverage> coverages
    ) {
        List<AnalystCoverageJpaEntity> entities = coverages.stream()
                .map(coverage -> {
                    var analyst = userRepository.findById(
                            coverage.analystId()
                    ).orElseThrow(
                            () -> new UserNotFoundException(
                                    coverage.analystId()
                            )
                    );

                    return mapper.toEntity(coverage, analyst);
                })
                .toList();

        return coverageRepository.saveAll(entities)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByAnalystId(UUID analystId) {
        coverageRepository.deleteByIdAnalystId(analystId);
    }
}