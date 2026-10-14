package com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request;

import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInternalUserRequest(

        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String name,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, message = "A senha deve possuir pelo menos 8 caracteres.")
        String password,

        @NotNull(message = "O papel do usuário é obrigatório.")
        UserRole role

) {
}
