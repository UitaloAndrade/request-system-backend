package com.uitalo.desafiobackend.requestsystem.domain.repository;

import com.uitalo.desafiobackend.requestsystem.domain.model.AuditLog;

public interface AuditLogRepository {

    void save(AuditLog event);
}
