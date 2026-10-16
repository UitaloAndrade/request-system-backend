package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationSearchPort;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class ElasticsearchSolicitationSearchAdapter implements SolicitationSearchPort {

    private static final String INDEX = "solicitations";
    private final ElasticsearchClient client;

    public ElasticsearchSolicitationSearchAdapter(ElasticsearchClient client) {
        this.client = client;
    }

    @Override
    public SearchPage search(SearchCriteria criteria) {
        try {
            Query query = buildQuery(criteria);
            int from = Math.multiplyExact(criteria.page(), criteria.size());
            SortOrder order = "asc".equalsIgnoreCase(criteria.sortDirection())
                    ? SortOrder.Asc
                    : SortOrder.Desc;

            SortOptions sort = SortOptions.of(s -> s.field(f ->
                    f.field(criteria.sortField()).order(order)));

            SearchResponse<SolicitationSearchDocument> response = client.search(
                    request -> request
                            .index(INDEX)
                            .query(query)
                            .from(from)
                            .size(criteria.size())
                            .sort(sort),
                    SolicitationSearchDocument.class
            );

            long total = client.count(count -> count.index(INDEX).query(query)).count();

            List<SearchItem> items = response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .filter(source -> source != null)
                    .map(SolicitationSearchDocument::toSearchItem)
                    .toList();

            return new SearchPage(items, criteria.page(), criteria.size(), total);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("A paginação solicitada é muito grande.", exception);
        } catch (Exception exception) {
            throw new ElasticsearchSearchException(
                    "Não foi possível consultar as solicitações no Elasticsearch.",
                    exception
            );
        }
    }

    private Query buildQuery(SearchCriteria criteria) {
        return Query.of(query -> query.bool(bool -> {
            if (criteria.q() != null && !criteria.q().isBlank()) {
                bool.must(must -> must.multiMatch(multi -> multi
                        .query(criteria.q())
                        .fields(
                                "title^3",
                                "description",
                                "street",
                                "neighborhood",
                                "city"
                        )));
            }

            if (criteria.status() != null) {
                bool.filter(filter -> filter.term(term -> term
                        .field("status")
                        .value(FieldValue.of(criteria.status()))));
            }

            if (criteria.serviceType() != null) {
                bool.filter(filter -> filter.term(term -> term
                        .field("serviceType")
                        .value(FieldValue.of(criteria.serviceType()))));
            }

            if (criteria.priority() != null) {
                bool.filter(filter -> filter.term(term -> term
                        .field("priority")
                        .value(FieldValue.of(criteria.priority()))));
            }

            // Sempre existe um filtro de UF imposto pelo serviço de aplicação.
            List<FieldValue> states = criteria.allowedStates().stream()
                    .map(FieldValue::of)
                    .toList();

            bool.filter(filter -> filter.terms(terms -> terms
                    .field("state")
                    .terms(values -> values.value(states))));

            if (criteria.dateFrom() != null || criteria.dateTo() != null) {
                bool.filter(filter -> filter.range(range -> range.date(date -> {
                    date.field("createdAt");
                    if (criteria.dateFrom() != null) {
                        date.gte(criteria.dateFrom().atStartOfDay().toInstant(ZoneOffset.UTC).toString());
                    }
                    if (criteria.dateTo() != null) {
                        date.lt(criteria.dateTo().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toString());
                    }
                    return date;
                })));
            }

            return bool;
        }));
    }
}
