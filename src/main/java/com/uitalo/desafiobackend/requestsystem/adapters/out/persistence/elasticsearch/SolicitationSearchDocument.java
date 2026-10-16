package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch;

import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationSearchPort;
import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeção de leitura. Não é entidade de domínio nem fonte oficial dos dados.
 */
@Document(indexName = "solicitations", createIndex = false)
public class SolicitationSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String clientId;

    @Field(type = FieldType.Keyword)
    private String serviceType;

    @Field(type = FieldType.Text, analyzer = "brazilian", searchAnalyzer = "brazilian")
    private String title;

    @Field(type = FieldType.Text, analyzer = "brazilian", searchAnalyzer = "brazilian")
    private String description;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String priority;

    @Field(type = FieldType.Keyword)
    private String cep;

    @Field(type = FieldType.Text, analyzer = "brazilian", searchAnalyzer = "brazilian")
    private String street;

    @Field(type = FieldType.Keyword)
    private String number;

    @Field(type = FieldType.Text, analyzer = "brazilian", searchAnalyzer = "brazilian")
    private String complement;

    @Field(type = FieldType.Text, analyzer = "brazilian", searchAnalyzer = "brazilian")
    private String neighborhood;

    @Field(type = FieldType.Text, analyzer = "brazilian", searchAnalyzer = "brazilian")
    private String city;

    @Field(type = FieldType.Keyword)
    private String state;

    @Field(type = FieldType.Date, format = DateFormat.strict_date)
    private LocalDate preferredDate;

    @Field(type = FieldType.Double)
    private BigDecimal estimatedValue;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant submittedAt;

    public SolicitationSearchDocument() {}

    public static SolicitationSearchDocument fromDomain(Solicitation solicitation) {
        SolicitationSearchDocument document = new SolicitationSearchDocument();
        document.id = solicitation.getId().toString();
        document.clientId = solicitation.getClientId().toString();
        document.serviceType = name(solicitation.getServiceType());
        document.title = solicitation.getTitle();
        document.description = solicitation.getDescription();
        document.status = name(solicitation.getStatus());
        document.priority = name(solicitation.getPriority());

        Address address = solicitation.getAddress();
        if (address != null) {
            document.cep = address.cep();
            document.street = address.street();
            document.number = address.number();
            document.complement = address.complement();
            document.neighborhood = address.neighborhood();
            document.city = address.city();
            document.state = address.state();
        }

        document.preferredDate = solicitation.getPreferredDate();
        document.estimatedValue = solicitation.getEstimatedValue();
        document.createdAt = solicitation.getCreatedAt();
        document.updatedAt = solicitation.getUpdatedAt();
        document.submittedAt = solicitation.getSubmittedAt();
        return document;
    }

    public SolicitationSearchPort.SearchItem toSearchItem() {
        return new SolicitationSearchPort.SearchItem(
                UUID.fromString(id),
                UUID.fromString(clientId),
                serviceType,
                title,
                description,
                status,
                priority,
                cep,
                street,
                number,
                complement,
                neighborhood,
                city,
                state,
                preferredDate,
                estimatedValue,
                createdAt,
                updatedAt,
                submittedAt
        );
    }

    private static String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getComplement() { return complement; }
    public void setComplement(String complement) { this.complement = complement; }
    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }
    public BigDecimal getEstimatedValue() { return estimatedValue; }
    public void setEstimatedValue(BigDecimal estimatedValue) { this.estimatedValue = estimatedValue; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}
