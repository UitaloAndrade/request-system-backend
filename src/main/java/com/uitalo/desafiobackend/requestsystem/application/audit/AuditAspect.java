package com.uitalo.desafiobackend.requestsystem.application.audit;

import com.uitalo.desafiobackend.requestsystem.domain.execption.InvalidCredentialsException;
import com.uitalo.desafiobackend.requestsystem.domain.model.AuditLog;
import com.uitalo.desafiobackend.requestsystem.domain.model.Solicitation;
import com.uitalo.desafiobackend.requestsystem.domain.model.User;
import com.uitalo.desafiobackend.requestsystem.domain.repository.AuditLogRepository;
import com.uitalo.desafiobackend.requestsystem.domain.repository.UserRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log =
            LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditAspect(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Around("@annotation(audited)")
    public Object audit(
            ProceedingJoinPoint joinPoint,
            Audited audited
    ) throws Throwable {

        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Throwable exception) {
            recordEvent(
                    joinPoint,
                    audited,
                    null,
                    "FAILURE",
                    "exception=" + exception.getClass().getSimpleName()
            );

            throw exception;
        }

        recordEvent(
                joinPoint,
                audited,
                result,
                "SUCCESS",
                null
        );

        return result;
    }

    private void recordEvent(
            ProceedingJoinPoint joinPoint,
            Audited audited,
            Object result,
            String outcome,
            String details
    ) {
        try {
            Actor actor = getActor();

            String resourceId = resolveResourceId(
                    joinPoint,
                    audited,
                    result
            );

            AuditLog event = new AuditLog(
                    UUID.randomUUID(),
                    actor.id(),
                    actor.name(),
                    audited.action(),
                    audited.resourceType(),
                    resourceId,
                    outcome,
                    details,
                    Instant.now()
            );

            auditLogRepository.save(event);

        } catch (RuntimeException auditException) {
            // A falha de auditoria não deve ocultar a exceção original
            // nem transformar uma operação de negócio bem-sucedida em erro.
            log.error(
                    "Não foi possível persistir o evento de auditoria "
                            + "da ação {}.",
                    audited.action(),
                    auditException
            );
        }
    }

    private String resolveResourceId(
            ProceedingJoinPoint joinPoint,
            Audited audited,
            Object result
    ) {
        if (audited.resourceIdFromResult()) {
            if (result instanceof Solicitation solicitation) {
                return solicitation.getId() == null
                        ? null
                        : solicitation.getId().toString();
            }

            return null;
        }

        Object[] arguments = joinPoint.getArgs();
        int index = audited.resourceIdParameter();

        if (index < 0 || index >= arguments.length) {
            return null;
        }

        Object value = arguments[index];

        return value == null ? null : value.toString();
    }

    private Actor getActor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return new Actor(null, "anonymous");
        }

        Object principal = authentication.getPrincipal();

        User user = userRepository.findById(parseUuid(principal)).get();

        String name = user.getName();

        UUID actorId = parseUuid(principal);

        if (actorId == null) {
            actorId = parseUuid(name);
        }

        return new Actor(actorId, name);
    }

    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private record Actor(UUID id, String name) {
    }
}