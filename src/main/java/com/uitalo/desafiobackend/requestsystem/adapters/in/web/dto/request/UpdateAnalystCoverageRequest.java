package com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateAnalystCoverageRequest(

        @NotEmpty(message = "Informe pelo menos uma UF de cobertura.")
        List<
            @NotBlank(message = "A UF não pode ser vazia.")
            @Size(min = 2, max = 2, message = "A UF deve ter duas letras.")
                    String
            > states

) {
}
