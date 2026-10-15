package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.solicitation;

import com.uitalo.desafiobackend.requestsystem.adapters.out.security.JwtAuthenticationFilter;
import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.*;
import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Priority;
import com.uitalo.desafiobackend.requestsystem.domain.model.ServiceType;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SolicitationController.class)
@AutoConfigureMockMvc(addFilters = false)
class SolicitationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateSolicitationUseCase createUseCase;

    @MockitoBean
    private GetSolicitationUseCase getUseCase;

    @MockitoBean
    private UpdateSolicitationBasicDataUseCase basicDataUseCase;

    @MockitoBean
    private UpdateSolicitationAddressUseCase addressUseCase;

    @MockitoBean
    private UpdateSolicitationDetailsUseCase detailsUseCase;

    @MockitoBean
    private SubmitSolicitationUseCase submitUseCase;

    // Evita dependência do filtro JWT real nesta fatia de teste MVC.
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UUID clientId = UUID.fromString(
            "11111111-1111-1111-1111-111111111111");

    private final UUID solicitationId = UUID.fromString(
            "22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUpAuthentication() {
        var authentication = new UsernamePasswordAuthenticationToken(
                clientId.toString(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateSolicitationAndReturn201() throws Exception {
        when(createUseCase.create(clientId))
                .thenReturn(newDraftSolicitation());

        mockMvc.perform(post("/solicitations"))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(solicitationId.toString()))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.currentStep").value(1));

        verify(createUseCase).create(clientId);
    }

    @Test
    void shouldGetSolicitationAndReturn200() throws Exception {
        when(getUseCase.getById(solicitationId, clientId))
                .thenReturn(newDraftSolicitation());

        mockMvc.perform(get("/solicitations/{id}", solicitationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitationId.toString()))
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        verify(getUseCase).getById(solicitationId, clientId);
    }

    @Test
    void shouldUpdateBasicDataAndReturn200() throws Exception {
        Solicitation solicitation = newDraftSolicitation();

        when(basicDataUseCase.update(
                eq(solicitationId),
                eq(clientId),
                eq(ServiceType.INSTALLATION),
                eq("Instalação de equipamento"),
                eq("Solicito a instalação de um equipamento no endereço informado.")
        )).thenReturn(solicitation);

        mockMvc.perform(patch(
                        "/solicitations/{id}/basic-data", solicitationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "serviceType": "INSTALLATION",
                      "title": "Instalação de equipamento",
                      "description": "Solicito a instalação de um equipamento no endereço informado."
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(solicitationId.toString()));

        verify(basicDataUseCase).update(
                solicitationId,
                clientId,
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado."
        );
    }

    @Test
    void shouldReturn400WhenBasicDataIsInvalid() throws Exception {
        mockMvc.perform(patch(
                        "/solicitations/{id}/basic-data", solicitationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "serviceType": "INSTALLATION",
                      "title": "AB",
                      "description": "Descrição suficientemente longa para o teste."
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(basicDataUseCase);
    }

    @Test
    void shouldUpdateAddressAndReturn200() throws Exception {
        Solicitation solicitation = newDraftSolicitation();

        when(addressUseCase.update(
                solicitationId, clientId, "70040-010", "12A", "Bloco A"))
                .thenReturn(solicitation);

        mockMvc.perform(patch(
                        "/solicitations/{id}/address", solicitationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "cep": "70040-010",
                      "number": "12A",
                      "complement": "Bloco A"
                    }
                    """))
                .andExpect(status().isOk());

        verify(addressUseCase).update(
                solicitationId, clientId, "70040-010", "12A", "Bloco A");
    }

    @Test
    void shouldUpdateDetailsAndReturn200() throws Exception {
        Solicitation solicitation = newDraftSolicitation();
        LocalDate preferredDate = LocalDate.now().plusDays(10);

        when(detailsUseCase.update(
                eq(solicitationId),
                eq(clientId),
                eq(Priority.MEDIUM),
                eq(preferredDate),
                eq(new BigDecimal("150.00")),
                eq(true)
        )).thenReturn(solicitation);

        mockMvc.perform(patch(
                        "/solicitations/{id}/details", solicitationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "priority": "MEDIUM",
                      "preferredDate": "%s",
                      "estimatedValue": 150.00,
                      "termsAccepted": true
                    }
                    """.formatted(preferredDate)))
                .andExpect(status().isOk());

        verify(detailsUseCase).update(
                solicitationId,
                clientId,
                Priority.MEDIUM,
                preferredDate,
                new BigDecimal("150.00"),
                true
        );
    }

    @Test
    void shouldSubmitSolicitationAndReturn200() throws Exception {
        Solicitation solicitation = newDraftSolicitation();
        solicitation.updateBasicData(
                ServiceType.INSTALLATION,
                "Instalação de equipamento",
                "Solicito a instalação de um equipamento no endereço informado.",
                Instant.now()
        );
        solicitation.updateAddress(
                new Address(
                        "70040010",
                        "Esplanada dos Ministérios",
                        "Brasília",
                        "Zona Cívico-Administrativa",
                        "DF",
                        "12A",
                        "Bloco A",
                        true
                ),
                Instant.now()
        );
        solicitation.confirmDetails(
                Priority.MEDIUM,
                LocalDate.now().plusDays(10),
                new BigDecimal("150.00"),
                true,
                LocalDate.now(),
                Instant.now()
        );
        solicitation.submit(Instant.now());

        when(submitUseCase.submit(solicitationId, clientId))
                .thenReturn(solicitation);

        mockMvc.perform(post(
                        "/solicitations/{id}/submit", solicitationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        verify(submitUseCase).submit(solicitationId, clientId);
    }

    private Solicitation newDraftSolicitation() {
        Instant now = Instant.now();
        return new Solicitation(solicitationId, clientId, now);
    }
}
