package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.user;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request.UpdateAnalystCoverageRequest;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.UpdateAnalystCoverageUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/analysts")
public class AnalystCoverageController {

    private final UpdateAnalystCoverageUseCase updateAnalystCoverageUseCase;

    public AnalystCoverageController(
            UpdateAnalystCoverageUseCase updateAnalystCoverageUseCase) {
        this.updateAnalystCoverageUseCase = updateAnalystCoverageUseCase;
    }

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
