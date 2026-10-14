package com.uitalo.desafiobackend.requestsystem.adpters.out.persistence.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AnalystCoverageJpaId implements Serializable {

    @Column(name = "analyst_id", nullable = false)
    private UUID analystId;

    @Column(name = "state", nullable = false, length = 2)
    private String state;

    protected AnalystCoverageJpaId() {
    }

    public AnalystCoverageJpaId(UUID analystId, String state) {
        this.analystId = analystId;
        this.state = state;
    }

    public UUID getAnalystId() {
        return analystId;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof AnalystCoverageJpaId other)) {
            return false;
        }

        return Objects.equals(analystId, other.analystId)
                && Objects.equals(state, other.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(analystId, state);
    }
}
