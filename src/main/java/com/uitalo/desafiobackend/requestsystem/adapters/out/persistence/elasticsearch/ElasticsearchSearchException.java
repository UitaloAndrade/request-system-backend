package com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch;

public class ElasticsearchSearchException extends RuntimeException {
    public ElasticsearchSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
