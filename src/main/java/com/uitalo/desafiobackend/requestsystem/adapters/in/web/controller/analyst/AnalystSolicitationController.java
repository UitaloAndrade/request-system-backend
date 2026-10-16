package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.analyst;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request.DecisionRequest;
import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response.AnalysisResponse;
import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.DecideSolicitationUseCase;
import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.GetSolicitationForAnalysisUseCase;
import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.StartAnalysisUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/analyst/solicitations")
@Tag(
        name = "Análise de solicitações",
        description = "Consulta, início da análise e decisão sobre solicitações por analistas."
)
public class AnalystSolicitationController {

    private final GetSolicitationForAnalysisUseCase getUseCase;
    private final StartAnalysisUseCase startUseCase;
    private final DecideSolicitationUseCase decideUseCase;

    public AnalystSolicitationController(
            GetSolicitationForAnalysisUseCase getUseCase,
            StartAnalysisUseCase startUseCase,
            DecideSolicitationUseCase decideUseCase
    ) {
        this.getUseCase = getUseCase;
        this.startUseCase = startUseCase;
        this.decideUseCase = decideUseCase;
    }

    @Operation(
            summary = "Consultar solicitação para análise",
            description = """
        Retorna os dados da solicitação para avaliação.
        O analista precisa possuir abrangência sobre a UF do endereço
        da solicitação.
        """
    )
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal
    ) {
        UUID analystId = UUID.fromString(principal);

        Solicitation solicitation = getUseCase.getById(id, analystId);

        return ResponseEntity.ok(AnalysisResponse.from(solicitation));
    }

    @Operation(
            summary = "Iniciar análise",
            description = """
        Inicia a análise de uma solicitação enviada pelo cliente,
        vinculando-a ao analista autenticado e alterando seu status
        para IN_REVIEW.
        """
    )
    @PostMapping("/{id}/start")
    public ResponseEntity<AnalysisResponse> start(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal
    ) {
        UUID analystId = UUID.fromString(principal);

        Solicitation solicitation = startUseCase.start(id, analystId);

        return ResponseEntity.ok(AnalysisResponse.from(solicitation));
    }

    @Operation(
            summary = "Decidir solicitação",
            description = """
        Registra a decisão de aprovação ou rejeição de uma solicitação
        em análise, incluindo o comentário justificativo do analista.
        """
    )
    @PostMapping("/{id}/decide")
    public ResponseEntity<AnalysisResponse> decide(
            @PathVariable UUID id,
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody DecisionRequest request
    ) {
        UUID analystId = UUID.fromString(principal);

        Solicitation solicitation = decideUseCase.decide(
                id,
                analystId,
                request.type(),
                request.comment()
        );

        return ResponseEntity.ok(AnalysisResponse.from(solicitation));
    }
}
