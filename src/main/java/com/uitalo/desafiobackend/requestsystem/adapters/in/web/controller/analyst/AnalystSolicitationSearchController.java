package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.analyst;

import com.uitalo.desafiobackend.requestsystem.application.port.out.SolicitationSearchPort;
import com.uitalo.desafiobackend.requestsystem.application.usecase.analyst.SearchSolicitationsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/analyst/solicitations")
@Tag(
        name = "Análise de solicitações",
        description = "Pesquisa paginada de solicitações dentro da abrangência do analista."
)
public class AnalystSolicitationSearchController {

    private final SearchSolicitationsUseCase searchUseCase;

    public AnalystSolicitationSearchController(
            SearchSolicitationsUseCase searchUseCase
    ) {
        this.searchUseCase = searchUseCase;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Pesquisar solicitações",
            description = """
                Pesquisa solicitações por texto, status, tipo de serviço,
                prioridade, UF e período de criação. O backend sempre limita
                os resultados às UFs cobertas pelo analista autenticado.
                Se a UF solicitada não estiver na cobertura, a resposta será vazia.
                """
    )
    public SolicitationSearchPort.SearchPage search(
            @Parameter(description = "Texto pesquisado em título, descrição e endereço.")
            @RequestParam(required = false) String q,

            @Parameter(description = "Status: DRAFT, SUBMITTED, IN_REVIEW, APPROVED ou REJECTED.")
            @RequestParam(required = false) String status,

            @Parameter(description = "Tipo: INSTALLATION, MAINTENANCE ou INSPECTION.")
            @RequestParam(required = false) String serviceType,

            @Parameter(description = "Prioridade: LOW, MEDIUM ou HIGH.")
            @RequestParam(required = false) String priority,

            @Parameter(description = "UF de dois caracteres, por exemplo DF.")
            @RequestParam(required = false) String state,

            @Parameter(description = "Data inicial inclusiva (yyyy-MM-dd), aplicada à data de criação.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "Data final inclusiva (yyyy-MM-dd), aplicada à data de criação.")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Campo e direção, por exemplo createdAt,desc. Campos permitidos: createdAt, updatedAt, submittedAt, preferredDate, status, priority.")
            @RequestParam(defaultValue = "createdAt,desc") String sort,

            @AuthenticationPrincipal String principal
    ) {
        UUID analystId = UUID.fromString(principal);
        return searchUseCase.search(
                q, status, serviceType, priority, state,
                dateFrom, dateTo, page, size, sort, analystId
        );
    }
}
