package com.uitalo.desafiobackend.requestsystem.adpters.in.web.controller;

import com.uitalo.desafiobackend.requestsystem.adpters.in.web.dto.request.RegisterClientRequest;
import com.uitalo.desafiobackend.requestsystem.adpters.in.web.dto.response.UserResponse;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.RegisterClientUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterClientUseCase registerClientUseCase;

    public AuthController(RegisterClientUseCase registerClientUseCase) {
        this.registerClientUseCase = registerClientUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterClientRequest request) {

        User user = registerClientUseCase.register(
                request.name(),
                request.email(),
                request.password()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(user));
    }
}