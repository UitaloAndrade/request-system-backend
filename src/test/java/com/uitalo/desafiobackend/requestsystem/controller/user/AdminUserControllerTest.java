package com.uitalo.desafiobackend.requestsystem.controller.user;

import com.uitalo.desafiobackend.requestsystem.adpters.in.web.controller.AdminUserController;
import com.uitalo.desafiobackend.requestsystem.adpters.in.web.exception.GlobalExceptionHandler;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.CreateInternalUserUseCase;
import com.uitalo.desafiobackend.requestsystem.config.TestSecurityConfig;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateInternalUserUseCase createInternalUserUseCase;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarAnalistaQuandoAdministradorEstiverAutenticado()
            throws Exception {

        User user = new User(
                UUID.randomUUID(),
                "João Analista",
                "joao@example.com",
                "hash-bcrypt",
                UserRole.ANALYST,
                true,
                Instant.now()
        );

        when(createInternalUserUseCase.create(
                "João Analista",
                "joao@example.com",
                "SenhaSegura123",
                UserRole.ANALYST
        )).thenReturn(user);

        mockMvc.perform(post("/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João Analista",
                                  "email": "joao@example.com",
                                  "password": "SenhaSegura123",
                                  "role": "ANALYST"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.name").value("João Analista"))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.role").value("ANALYST"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(createInternalUserUseCase).create(
                "João Analista",
                "joao@example.com",
                "SenhaSegura123",
                UserRole.ANALYST
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarAdministradorQuandoPapelForAdmin() throws Exception {
        User user = new User(
                UUID.randomUUID(),
                "Ana Administradora",
                "ana@example.com",
                "hash-bcrypt",
                UserRole.ADMIN,
                true,
                Instant.now()
        );

        when(createInternalUserUseCase.create(
                "Ana Administradora",
                "ana@example.com",
                "SenhaSegura123",
                UserRole.ADMIN
        )).thenReturn(user);

        mockMvc.perform(post("/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ana Administradora",
                                  "email": "ana@example.com",
                                  "password": "SenhaSegura123",
                                  "role": "ADMIN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void deveNegarCadastroInternoParaUsuarioClient() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João Analista",
                                  "email": "joao@example.com",
                                  "password": "SenhaSegura123",
                                  "role": "ANALYST"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(createInternalUserUseCase);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarBadRequestQuandoPapelNaoForInformado()
            throws Exception {

        mockMvc.perform(post("/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João Analista",
                                  "email": "joao@example.com",
                                  "password": "SenhaSegura123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.role").exists());

        verifyNoInteractions(createInternalUserUseCase);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarBadRequestQuandoEmailForInvalido()
            throws Exception {

        mockMvc.perform(post("/admin/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João Analista",
                                  "email": "email-invalido",
                                  "password": "SenhaSegura123",
                                  "role": "ANALYST"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verifyNoInteractions(createInternalUserUseCase);
    }
}
