package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.user;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request.LoginRequest;
import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request.RegisterClientRequest;
import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response.LoginResponse;
import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response.UserResponse;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.LoginUseCase;
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
    private final LoginUseCase loginUseCase;

    public AuthController(RegisterClientUseCase registerClientUseCase,
                          LoginUseCase loginUseCase) {
        this.registerClientUseCase = registerClientUseCase;
        this.loginUseCase = loginUseCase;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        var result = loginUseCase.login(
                request.email(),
                request.password()
        );

        var response = new LoginResponse(
                result.accessToken(),
                "Bearer",
                result.expiresIn(),
                UserResponse.from(result.user())
        );

        return ResponseEntity.ok(response);
    }
}