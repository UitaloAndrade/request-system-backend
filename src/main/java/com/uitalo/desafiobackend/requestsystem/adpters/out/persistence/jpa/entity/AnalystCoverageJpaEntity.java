package com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "analyst_coverage")
public class AnalystCoverageJpaEntity {

    @EmbeddedId
    private AnalystCoverageJpaId id;

    @MapsId("analystId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analyst_id", nullable = false)
    private UserJpaEntity analyst;

    protected AnalystCoverageJpaEntity() {
    }

    public AnalystCoverageJpaEntity(
            AnalystCoverageJpaId id,
            UserJpaEntity analyst
    ) {
        this.id = id;
        this.analyst = analyst;
    }

    public AnalystCoverageJpaId getId() {
        return id;
    }

    public UserJpaEntity getAnalyst() {
        return analyst;
    }
}