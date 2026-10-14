package com.uitalo.desafiobackend.requestsystem.application.port.out;

import com.uitalo.desafiobackend.requestsystem.domain.model.User;

public interface TokenProvider {

    String generateToken(User user);

    long getExpirationSeconds();
}