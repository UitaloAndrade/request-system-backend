package com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch.ElasticsearchSearchException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return error(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "A requisição contém campos inválidos.",
                request,
                errors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String field = violation.getPropertyPath() == null
                    ? "parameter"
                    : violation.getPropertyPath().toString();
            errors.putIfAbsent(field, violation.getMessage());
        }
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Um ou mais parâmetros não são válidos.", request, errors);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(
            HandlerMethodValidationException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Um ou mais parâmetros da requisição não são válidos.", request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "O corpo da requisição está ausente, malformado ou contém valores incompatíveis.",
                request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        String parameter = exception.getName();
        return error(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
                "O parâmetro '" + parameter + "' possui um formato ou valor inválido.",
                request, Map.of(parameter, "Verifique o tipo e o formato do valor informado."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        String parameter = exception.getParameterName();
        return error(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                "Um parâmetro obrigatório não foi informado.", request,
                Map.of(parameter, "O parâmetro é obrigatório."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "O método HTTP utilizado não é permitido para este recurso.", request, Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "O formato do conteúdo enviado não é suportado. Verifique o cabeçalho Content-Type.",
                request, Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_ACCEPTABLE, "NOT_ACCEPTABLE",
                "A API não consegue produzir uma resposta no formato solicitado.", request, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
                "O endpoint solicitado não foi encontrado.", request, Map.of());
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION",
                exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT",
                safeMessage(exception, "Um ou mais argumentos informados são inválidos."),
                request, Map.of());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException exception,
            HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "BUSINESS_STATE_CONFLICT",
                safeMessage(exception, "A operação não é permitida no estado atual do recurso."),
                request, Map.of());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(SolicitationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSolicitationNotFound(
            SolicitationNotFoundException exception,
            HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "SOLICITATION_NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(AnalystCoverageDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAnalystCoverageDenied(
            AnalystCoverageDeniedException exception,
            HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(PostalCodeServiceException.class)
    public ResponseEntity<ErrorResponse> handlePostalCodeService(
            PostalCodeServiceException exception,
            HttpServletRequest request) {
        return error(HttpStatus.BAD_GATEWAY, "POSTAL_CODE_SERVICE_UNAVAILABLE",
                "Não foi possível consultar o serviço de CEP no momento.", request, Map.of());
    }

    @ExceptionHandler(ElasticsearchSearchException.class)
    public ResponseEntity<ErrorResponse> handleElasticsearch(
            ElasticsearchSearchException exception,
            HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "SEARCH_SERVICE_UNAVAILABLE",
                "O serviço de pesquisa está temporariamente indisponível. Tente novamente mais tarde.",
                request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.", request, Map.of());
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> details) {
        return ResponseEntity.status(status).body(
                ErrorResponse.of(status, code, message, request.getRequestURI(), details));
    }

    private String safeMessage(RuntimeException exception, String fallback) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }
}
