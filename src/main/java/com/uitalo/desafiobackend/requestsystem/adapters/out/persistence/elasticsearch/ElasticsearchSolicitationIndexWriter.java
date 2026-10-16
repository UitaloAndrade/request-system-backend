package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchSolicitationIndexWriter {

    private static final String INDEX = "solicitations";
    private final ElasticsearchClient client;

    public ElasticsearchSolicitationIndexWriter(ElasticsearchClient client) {
        this.client = client;
    }

    public void index(Solicitation solicitation) {
        SolicitationSearchDocument document =
                SolicitationSearchDocument.fromDomain(solicitation);

        try {
            client.index(request -> request
                    .index(INDEX)
                    .id(document.getId())
                    .document(document));
        } catch (IOException exception) {
            throw new ElasticsearchSearchException(
                    "Falha ao indexar a solicitação " + solicitation.getId(),
                    exception
            );
        }
    }
}
