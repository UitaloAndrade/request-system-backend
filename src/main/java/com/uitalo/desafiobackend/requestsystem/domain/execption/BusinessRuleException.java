package com.uitalo.desafiobackend.requestsystem.domain.execption;

public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
