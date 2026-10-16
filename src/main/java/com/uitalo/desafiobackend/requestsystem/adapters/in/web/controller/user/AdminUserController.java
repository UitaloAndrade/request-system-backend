package com.uitalo.desafiobackend.requestsystem.adapters.in.web.controller.user;

import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.request.CreateInternalUserRequest;
import com.uitalo.desafiobackend.requestsystem.adapters.in.web.dto.response.UserResponse;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.CreateInternalUserUseCase;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@Tag(
        name = "Administração de usuários",
        description = "Gerenciamento de usuários internos da plataforma."
)
public class AdminUserController {

    private final CreateInternalUserUseCase createInternalUserUseCase;

    public AdminUserController(
            CreateInternalUserUseCase createInternalUserUseCase) {
        this.createInternalUserUseCase = createInternalUserUseCase;
    }

    @Operation(
            summary = "Criar usuário interno",
            description = """
        Cria um usuário interno com perfil ANALYST ou ADMIN.
        Operação exclusiva para usuários com perfil de administrador.
        """
    )
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
