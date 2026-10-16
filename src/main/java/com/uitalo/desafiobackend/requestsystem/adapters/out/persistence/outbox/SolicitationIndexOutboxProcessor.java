package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.outbox;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch.ElasticsearchSolicitationIndexWriter;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.repository.SolicitationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class SolicitationIndexOutboxProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(SolicitationIndexOutboxProcessor.class);

    private final SpringDataSolicitationIndexOutboxRepository outboxRepository;
    private final SolicitationRepository solicitationRepository;
    private final ElasticsearchSolicitationIndexWriter indexWriter;

    public SolicitationIndexOutboxProcessor(
            SpringDataSolicitationIndexOutboxRepository outboxRepository,
            SolicitationRepository solicitationRepository,
            ElasticsearchSolicitationIndexWriter indexWriter
    ) {
        this.outboxRepository = outboxRepository;
        this.solicitationRepository = solicitationRepository;
        this.indexWriter = indexWriter;
    }

    @Scheduled(fixedDelayString = "${app.elasticsearch.outbox-delay-ms:2000}")
    @Transactional
    public void processPendingEvents() {
        List<SolicitationIndexOutboxJpaEntity> pending =
                outboxRepository.findPending(Instant.now(), PageRequest.of(0, 100));

        for (SolicitationIndexOutboxJpaEntity event : pending) {
            try {
                Solicitation solicitation = solicitationRepository
                        .findById(event.getSolicitationId())
                        .orElse(null);

                // Solicitações removidas (se exclusão for implementada no futuro)
                // não devem bloquear indefinidamente a fila.
                if (solicitation != null) {
                    indexWriter.index(solicitation);
                }

                event.markProcessed(Instant.now());
            } catch (Exception exception) {
                event.registerFailure(exception.getClass().getSimpleName()
                        + ": " + exception.getMessage());
                log.warn(
                        "Falha ao indexar solicitação {}. Tentativa {}.",
                        event.getSolicitationId(),
                        event.getAttempts(),
                        exception
                );
            }
        }
        outboxRepository.saveAll(pending);
    }
}
