package com.uitalo.desafiobackend.requestsystem.domain.execption;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos.");
    }
}
