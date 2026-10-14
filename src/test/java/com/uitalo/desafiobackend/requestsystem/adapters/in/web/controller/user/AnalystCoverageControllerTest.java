//package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.user;
//
//import com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception.GlobalExceptionHandler;
//import com.uitalo.desafiobackend.requestsystem.application.usecase.user.UpdateAnalystCoverageUseCase;
//import com.uitalo.desafiobackend.requestsystem.infrastructure.config.security.TestSecurityConfig;
//import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.UUID;
//
//import static org.mockito.Mockito.*;
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(AnalystCoverageController.class)
//@ActiveProfiles("test")
//@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
//class AnalystCoverageControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private UpdateAnalystCoverageUseCase updateAnalystCoverageUseCase;
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void deveAtualizarCoberturaDoAnalistaComSucesso() throws Exception {
//        UUID analystId = UUID.randomUUID();
//
//        doNothing().when(updateAnalystCoverageUseCase)
//                .updateCoverage(analystId, java.util.List.of("DF", "GO"));
//
//        mockMvc.perform(put("/admin/analysts/{analystId}/coverage", analystId)
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "states": ["DF", "GO"]
//                                }
//                                """))
//                .andExpect(status().isNoContent())
//                .andExpect(content().string(""));
//
//        verify(updateAnalystCoverageUseCase)
//                .updateCoverage(analystId, java.util.List.of("DF", "GO"));
//    }
//
//    @Test
//    @WithMockUser(roles = "CLIENT")
//    void deveNegarAtualizacaoDaCoberturaParaCliente() throws Exception {
//        UUID analystId = UUID.randomUUID();
//
//        mockMvc.perform(put("/admin/analysts/{analystId}/coverage", analystId)
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "states": ["DF", "GO"]
//                                }
//                                """))
//                .andExpect(status().isForbidden());
//
//        verifyNoInteractions(updateAnalystCoverageUseCase);
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void deveRetornarBadRequestQuandoListaDeUfsEstiverVazia()
//            throws Exception {
//
//        UUID analystId = UUID.randomUUID();
//
//        mockMvc.perform(put("/admin/analysts/{analystId}/coverage", analystId)
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "states": []
//                                }
//                                """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.states").exists());
//
//        verifyNoInteractions(updateAnalystCoverageUseCase);
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void deveRetornarBadRequestQuandoUfTiverFormatoInvalido()
//            throws Exception {
//
//        UUID analystId = UUID.randomUUID();
//
//        mockMvc.perform(put("/admin/analysts/{analystId}/coverage", analystId)
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "states": ["DF", "GOO"]
//                                }
//                                """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors['states[1]']").exists());
//
//        verifyNoInteractions(updateAnalystCoverageUseCase);
//    }
//
//    @Test
//    @WithMockUser(roles = "ADMIN")
//    void deveRetornarBadRequestQuandoUfNaoExistir()
//            throws Exception {
//
//        UUID analystId = UUID.randomUUID();
//
//        doThrow(new BusinessRuleException("UF inválida: XX"))
//                .when(updateAnalystCoverageUseCase)
//                .updateCoverage(analystId, java.util.List.of("XX"));
//
//        mockMvc.perform(put("/admin/analysts/{analystId}/coverage", analystId)
//                        .contentType(APPLICATION_JSON)
//                        .content("""
//                                {
//                                  "states": ["XX"]
//                                }
//                                """))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.message").value("UF inválida: XX"));
//    }
//}
