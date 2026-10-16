package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.solicitation;

import com.uitalo.desafiobackend.requestsystem.application.usecase.solicitation.*;
import com.uitalo.desafiobackend.requestsystem.domain.model.Address;
import com.uitalo.desafiobackend.requestsystem.domain.model.Priority;
import com.uitalo.desafiobackend.requestsystem.domain.model.ServiceType;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/solicitations")
@Tag(
        name = "Solicitações",
        description = "Criação, edição, consulta e envio de solicitações de atendimento."
)
public class SolicitationController {

    private final CreateSolicitationUseCase createUseCase;
    private final GetSolicitationUseCase getUseCase;
    private final UpdateSolicitationBasicDataUseCase basicDataUseCase;
    private final UpdateSolicitationAddressUseCase addressUseCase;
    private final UpdateSolicitationDetailsUseCase detailsUseCase;
    private final SubmitSolicitationUseCase submitUseCase;

    public SolicitationController(
            CreateSolicitationUseCase createUseCase,
            GetSolicitationUseCase getUseCase,
            UpdateSolicitationBasicDataUseCase basicDataUseCase,
            UpdateSolicitationAddressUseCase addressUseCase,
            UpdateSolicitationDetailsUseCase detailsUseCase,
            SubmitSolicitationUseCase submitUseCase) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
        this.basicDataUseCase = basicDataUseCase;
        this.addressUseCase = addressUseCase;
        this.detailsUseCase = detailsUseCase;
        this.submitUseCase = submitUseCase;
    }

    @Operation(
            summary = "Criar solicitação",
            description = """
        Cria uma nova solicitação em rascunho vinculada ao cliente autenticado.
        O identificador do cliente é obtido a partir do contexto de autenticação.
        """
    )
    @PostMapping
    public ResponseEntity<SolicitationResponse> create(
            @AuthenticationPrincipal String principal) {

        UUID clientId = authenticatedUserId(principal);
        Solicitation solicitation = createUseCase.create(clientId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(SolicitationResponse.from(solicitation));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitationResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal) {

        Solicitation solicitation = getUseCase.getById(
                id, authenticatedUserId(principal));

        return ResponseEntity.ok(SolicitationResponse.from(solicitation));
    }

    @PatchMapping("/{id}/basic-data")
    public ResponseEntity<SolicitationResponse> updateBasicData(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody BasicDataRequest request) {

        Solicitation solicitation = basicDataUseCase.update(
                id,
                authenticatedUserId(principal),
                request.serviceType(),
                request.title(),
                request.description()
        );

        return ResponseEntity.ok(SolicitationResponse.from(solicitation));
    }

    @Operation(
            summary = "Atualizar endereço",
            description = """
        Atualiza o endereço da solicitação em rascunho.
        Os dados de logradouro, bairro, cidade e UF são consultados
        a partir do CEP informado por meio da integração com o ViaCEP.
        """
    )
    @PatchMapping("/{id}/address")
    public ResponseEntity<SolicitationResponse> updateAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody AddressRequest request) {

        Solicitation solicitation = addressUseCase.update(
                id,
                authenticatedUserId(principal),
                request.cep(),
                request.number(),
                request.complement()
        );

        return ResponseEntity.ok(SolicitationResponse.from(solicitation));
    }

    @PatchMapping("/{id}/details")
    public ResponseEntity<SolicitationResponse> updateDetails(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody DetailsRequest request) {

        Solicitation solicitation = detailsUseCase.update(
                id,
                authenticatedUserId(principal),
                request.priority(),
                request.preferredDate(),
                request.estimatedValue(),
                request.termsAccepted()
        );

        return ResponseEntity.ok(SolicitationResponse.from(solicitation));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SolicitationResponse> submit(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal) {

        Solicitation solicitation = submitUseCase.submit(
                id, authenticatedUserId(principal));

        return ResponseEntity.ok(SolicitationResponse.from(solicitation));
    }

    private UUID authenticatedUserId(String principal) {
        if (principal == null || principal.isBlank()) {
            throw new IllegalStateException(
                    "Não foi possível identificar o usuário autenticado.");
        }
        return UUID.fromString(principal);
    }

    public record BasicDataRequest(
            @NotNull(message = "O tipo de serviço é obrigatório.")
            ServiceType serviceType,

            @NotBlank(message = "O título é obrigatório.")
            @Size(min = 3, max = 80,
                    message = "O título deve ter entre 3 e 80 caracteres.")
            String title,

            @NotBlank(message = "A descrição é obrigatória.")
            @Size(min = 20, max = 1000,
                    message = "A descrição deve ter entre 20 e 1000 caracteres.")
            String description
    ) {}

    public record AddressRequest(
            @NotBlank(message = "O CEP é obrigatório.")
            @Pattern(regexp = "\\d{5}-?\\d{3}",
                    message = "O CEP deve conter oito dígitos.")
            String cep,

            @NotBlank(message = "O número do endereço é obrigatório.")
            @Size(max = 20,
                    message = "O número deve ter no máximo 20 caracteres.")
            String number,

            @Size(max = 100,
                    message = "O complemento deve ter no máximo 100 caracteres.")
            String complement
    ) {}

    public record DetailsRequest(
            @NotNull(message = "A prioridade é obrigatória.")
            Priority priority,

            @NotNull(message = "A data desejada é obrigatória.")
            LocalDate preferredDate,

            @NotNull(message = "O valor estimado é obrigatório.")
            BigDecimal estimatedValue,

            @NotNull(message = "É necessário informar o aceite dos termos.")
            Boolean termsAccepted
    ) {}

    public record SolicitationResponse(
            UUID id,
            UUID clientId,
            ServiceType serviceType,
            String title,
            String description,
            Address address,
            Priority priority,
            LocalDate preferredDate,
            BigDecimal estimatedValue,
            Boolean termsAccepted,
            String status,
            int currentStep,
            UUID analystId,
            Instant createdAt,
            Instant updatedAt,
            Instant submittedAt
    ) {
        public static SolicitationResponse from(Solicitation solicitation) {
            return new SolicitationResponse(
                    solicitation.getId(),
                    solicitation.getClientId(),
                    solicitation.getServiceType(),
                    solicitation.getTitle(),
                    solicitation.getDescription(),
                    solicitation.getAddress(),
                    solicitation.getPriority(),
                    solicitation.getPreferredDate(),
                    solicitation.getEstimatedValue(),
                    solicitation.getTermsAccepted(),
                    solicitation.getStatus().name(),
                    solicitation.getCurrentStep(),
                    solicitation.getAnalystId(),
                    solicitation.getCreatedAt(),
                    solicitation.getUpdatedAt(),
                    solicitation.getSubmittedAt()
            );
        }
    }
}
