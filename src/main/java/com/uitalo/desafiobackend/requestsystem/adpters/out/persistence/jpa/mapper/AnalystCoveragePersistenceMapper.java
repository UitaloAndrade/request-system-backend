package com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.mapper;

import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity.AnalystCoverageJpaEntity;
import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity.AnalystCoverageJpaId;
import com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity.UserJpaEntity;
import com.uitalo.desafiobackend.requestsystem.domain.model.AnalystCoverage;
import org.springframework.stereotype.Component;

@Component
public class AnalystCoveragePersistenceMapper {

    public AnalystCoverage toDomain(
            AnalystCoverageJpaEntity entity
    ) {
        return new AnalystCoverage(
                entity.getId().getAnalystId(),
                entity.getId().getState()
        );
    }

    public AnalystCoverageJpaEntity toEntity(
            AnalystCoverage coverage
    ) {
        throw new UnsupportedOperationException(
                "Utilize toEntity(coverage, analyst) para associar o usuário."
        );
    }

    public AnalystCoverageJpaEntity toEntity(
            AnalystCoverage coverage,
            UserJpaEntity analyst
    ) {
        AnalystCoverageJpaId id = new AnalystCoverageJpaId(
                coverage.analystId(),
                coverage.state()
        );

        return new AnalystCoverageJpaEntity(id, analyst);
    }
}
