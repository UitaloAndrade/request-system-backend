package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.user;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request.UpdateAnalystCoverageRequest;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.UpdateAnalystCoverageUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/analysts")
@Tag(
        name = "Abrangência dos analistas",
        description = "Configuração das unidades federativas atendidas por cada analista."
)
public class AnalystCoverageController {

    private final UpdateAnalystCoverageUseCase updateAnalystCoverageUseCase;

    public AnalystCoverageController(
            UpdateAnalystCoverageUseCase updateAnalystCoverageUseCase) {
        this.updateAnalystCoverageUseCase = updateAnalystCoverageUseCase;
    }

    @Operation(
            summary = "Atualizar abrangência do analista",
            description = """
        Define ou substitui as UFs nas quais o analista está autorizado
        a atuar. Operação exclusiva para administradores.
        """
    )
    @PutMapping("/{analystId}/coverage")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateCoverage(
            @PathVariable UUID analystId,
            @Valid @RequestBody UpdateAnalystCoverageRequest request) {

        updateAnalystCoverageUseCase.updateCoverage(
                analystId,
                request.states()
        );

        return ResponseEntity.noContent().build();
    }
}
