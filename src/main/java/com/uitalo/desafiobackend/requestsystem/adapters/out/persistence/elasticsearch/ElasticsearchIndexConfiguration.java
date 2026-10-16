package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

@Configuration
public class ElasticsearchIndexConfiguration {

    @Bean
    ApplicationRunner createSolicitationsIndex(ElasticsearchOperations operations) {
        return args -> {
            IndexOperations indexOperations =
                    operations.indexOps(SolicitationSearchDocument.class);

            if (!indexOperations.exists()) {
                indexOperations.createWithMapping();
            }
        };
    }
}
