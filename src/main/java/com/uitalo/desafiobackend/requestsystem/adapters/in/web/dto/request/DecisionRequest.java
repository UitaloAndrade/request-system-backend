package com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request;

import com.uitalo.desafiobackend.requestsystem.domain.model.DecisionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisionRequest(

        @NotNull(message = "O tipo da decisão é obrigatório.")
        DecisionType type,

        @NotBlank(message = "A justificativa da decisão é obrigatória.")
        @Size(
                min = 10,
                max = 1000,
                message = "A justificativa deve ter entre 10 e 1000 caracteres."
        )
        String comment

) {
}
