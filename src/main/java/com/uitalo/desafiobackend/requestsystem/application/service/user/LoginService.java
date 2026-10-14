package com.uitalo.desafiobackend.requestsystem.application.service.user;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PasswordHasher;
import com.uitalo.desafiobackend.requestsystem.application.port.out.TokenProvider;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.LoginUseCase;
import com.uitalo.desafiobackend.requestsystem.application.usecase.user.dto.LoginResult;
import com.uitalo.desafiobackend.requestsystem.domain.execption.InvalidCredentialsException;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public LoginService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(String email, String password) {
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new InvalidCredentialsException();
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()
                || !passwordHasher.matches(
                password,
                user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user);

        return new LoginResult(
                user,
                token,
                tokenProvider.getExpirationSeconds()
        );
    }
}
