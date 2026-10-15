package com.uitalo.desafiobackend.requestsystem.domain.execption;

public class PostalCodeServiceException extends RuntimeException {

    public PostalCodeServiceException(String message) {
        super(message);
    }

    public PostalCodeServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}