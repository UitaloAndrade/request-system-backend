//package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.user;
//
//import com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception.GlobalExceptionHandler;
//import com.uitalo.desafiobackend.requestsystem.application.usecase.user.LoginUseCase;
//import com.uitalo.desafiobackend.requestsystem.application.usecase.user.RegisterClientUseCase;
//import com.uitalo.desafiobackend.requestsystem.application.usecase.user.dto.LoginResult;
//import com.uitalo.desafiobackend.requestsystem.domain.execption.InvalidCredentialsException;
//import com.uitalo.desafiobackend.requestsystem.infrastructure.config.security.TestSecurityConfig;
//import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
//import com.uitalo.desafiobackend.requestsystem.domain.model.User;
//import com.uitalo.desafiobackend.requestsystem.domain.model.UserRole;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.Instant;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AuthController.class)
//@ActiveProfiles("test")
//@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
//class AuthControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private RegisterClientUseCase registerClientUseCase;
//
//    @MockitoBean
//    private LoginUseCase loginUseCase;
//
//    @Test
//    void deveCadastrarClienteComSucesso() throws Exception {
//        User user = new User(
//                UUID.randomUUID(),
//                "Maria da Silva",
//                "maria@example.com",
//                "hash-bcrypt",
//                UserRole.CLIENT,
//                true,
//                Instant.now()
//        );
//
//        when(registerClientUseCase.register(
//                "Maria da Silva",
//                "maria@example.com",
//                "SenhaSegura123"
//        )).thenReturn(user);
//
//        mockMvc.perform(post("/auth/register")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "name": "Maria da Silva",
//                                  "email": "maria@example.com",
//                                  "password": "SenhaSegura123"
//                                }
//                                """))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value(user.getId().toString()))
//                .andExpect(jsonPath("$.name").value("Maria da Silva"))
//                .andExpect(jsonPath("$.email").value("maria@example.com"))
//                .andExpect(jsonPath("$.role").value("CLIENT"))
//                .andExpect(jsonPath("$.active").value(true))
//                .andExpect(jsonPath("$.passwordHash").doesNotExist())
//                .andExpect(jsonPath("$.password").doesNotExist());
//
//        verify(registerClientUseCase).register(
//                "Maria da Silva",
//                "maria@example.com",
//                "SenhaSegura123"
//        );
//    }
//
//    @Test
//    void deveRetornarBadRequestQuandoEmailForInvalido() throws Exception {
//        mockMvc.perform(post("/auth/register")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "name": "Maria da Silva",
//                                  "email": "email-invalido",
//                                  "password": "SenhaSegura123"
//                                }
//                                """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").exists())
//                .andExpect(jsonPath("$.errors.email").exists());
//
//        verifyNoInteractions(registerClientUseCase);
//    }
//
//    @Test
//    void deveRetornarBadRequestQuandoSenhaForCurta() throws Exception {
//        mockMvc.perform(post("/auth/register")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "name": "Maria da Silva",
//                                  "email": "maria@example.com",
//                                  "password": "123"
//                                }
//                                """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.password").exists());
//
//        verifyNoInteractions(registerClientUseCase);
//    }
//
//    @Test
//    void deveRetornarBadRequestQuandoNomeEstiverVazio() throws Exception {
//        mockMvc.perform(post("/auth/register")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "name": " ",
//                                  "email": "maria@example.com",
//                                  "password": "SenhaSegura123"
//                                }
//                                """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.name").exists());
//
//        verifyNoInteractions(registerClientUseCase);
//    }
//
//    @Test
//    void deveRetornarBadRequestQuandoCadastroViolarRegraDeNegocio()
//            throws Exception {
//
//        when(registerClientUseCase.register(anyString(), anyString(), anyString()))
//                .thenThrow(new BusinessRuleException(
//                        "Já existe um usuário cadastrado com este e-mail."
//                ));
//
//        mockMvc.perform(post("/auth/register")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "name": "Maria da Silva",
//                                  "email": "maria@example.com",
//                                  "password": "SenhaSegura123"
//                                }
//                                """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message")
//                        .value("Já existe um usuário cadastrado com este e-mail."));
//    }
//
//    @Test
//    void deveRealizarLoginComSucesso() throws Exception {
//        User user = new User(
//                UUID.randomUUID(),
//                "Maria da Silva",
//                "maria@example.com",
//                "hash-bcrypt",
//                UserRole.CLIENT,
//                true,
//                Instant.now()
//        );
//
//        when(loginUseCase.login("maria@example.com", "SenhaSegura123"))
//                .thenReturn(new LoginResult(user, "jwt-de-teste", 3600L));
//
//        mockMvc.perform(post("/auth/login")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                            {
//                              "email": "maria@example.com",
//                              "password": "SenhaSegura123"
//                            }
//                            """))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("jwt-de-teste"))
//                .andExpect(jsonPath("$.tokenType").value("Bearer"))
//                .andExpect(jsonPath("$.expiresIn").value(3600))
//                .andExpect(jsonPath("$.user.email").value("maria@example.com"))
//                .andExpect(jsonPath("$.user.role").value("CLIENT"))
//                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
//
//        verify(loginUseCase).login("maria@example.com", "SenhaSegura123");
//    }
//
//    @Test
//    void deveRetornarUnauthorizedQuandoCredenciaisForemInvalidas()
//            throws Exception {
//
//        when(loginUseCase.login("maria@example.com", "senha-errada"))
//                .thenThrow(new InvalidCredentialsException());
//
//        mockMvc.perform(post("/auth/login")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                            {
//                              "email": "maria@example.com",
//                              "password": "senha-errada"
//                            }
//                            """))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.message")
//                        .value("E-mail ou senha inválidos."));
//    }
//
//    @Test
//    void deveRejeitarLoginComEmailInvalido() throws Exception {
//        mockMvc.perform(post("/auth/login")
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                            {
//                              "email": "email-invalido",
//                              "password": "SenhaSegura123"
//                            }
//                            """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.email").exists());
//
//        verifyNoInteractions(loginUseCase);
//    }
//}
