package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface SpringDataSolicitationIndexOutboxRepository
        extends JpaRepository<SolicitationIndexOutboxJpaEntity, Long> {

    @Query("""
            select event
            from SolicitationIndexOutboxJpaEntity event
            where event.processedAt is null
              and (event.nextAttemptAt is null or event.nextAttemptAt <= :now)
            order by event.createdAt asc
            """)
    List<SolicitationIndexOutboxJpaEntity> findPending(
            @Param("now") Instant now,
            Pageable pageable
    );
}
