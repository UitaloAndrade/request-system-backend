package com.uitalo.desafiobackend.requestsystem.adpters.out.security;

import com.uitalo.desafiobackend.requestsystem.application.port.out.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHasher(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(
            String rawPassword,
            String encodedPassword
    ) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}