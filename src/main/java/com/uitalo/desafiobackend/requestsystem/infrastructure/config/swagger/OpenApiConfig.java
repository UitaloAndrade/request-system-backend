package com.uitalo.desafiobackend.requestsystem.infrastructure.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Solicitações de Atendimento",
                version = "1.0.0",
                description = """
            API para cadastro, acompanhamento e análise de solicitações
            de atendimento, com controle de acesso por perfil de usuário.
            """,
                contact = @Contact(
                        name = "Equipe de Desenvolvimento"
                )
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Informe o token JWT obtido no endpoint /auth/login."
)
public class OpenApiConfig {
}
