package com.uitalo.desafiobackend.requestsystem.adapters.out.viacep;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViaCepResponse(
        String cep,
        String logradouro,
        String bairro,
        @JsonProperty("localidade") String cidade,
        String uf,
        Boolean erro
) {}
