package com.uitalo.desafiobackend.requestsystem.adpters.in.web.controller;

import com.uitalo.desafiobackend.requestsystem.adpters.in.web.dto.request.CreateInternalUserRequest;
import com.uitalo.desafiobackend.requestsystem.adpters.in.web.dto.response.UserResponse;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.CreateInternalUserUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final CreateInternalUserUseCase createInternalUserUseCase;

    public AdminUserController(
            CreateInternalUserUseCase createInternalUserUseCase) {
        this.createInternalUserUseCase = createInternalUserUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateInternalUserRequest request) {

        User user = createInternalUserUseCase.create(
                request.name(),
                request.email(),
                request.password(),
                request.role()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }
}
