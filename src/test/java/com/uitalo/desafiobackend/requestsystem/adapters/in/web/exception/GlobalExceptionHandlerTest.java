package com.uitalo.desafiobackend.requestsystem.adapters.in.web.exception;

import com.uitalo.desafiobackend.requestsystem.adapters.out.persistence.elasticsearch.ElasticsearchSearchException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.AnalystCoverageDeniedException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.BusinessRuleException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.InvalidCredentialsException;
import com.uitalo.desafiobackend.requestsystem.domain.execption.SolicitationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/solicitations");
    }

    @Test
    void deveRetornarContratoPadraoParaErroDeRegraDeNegocio() {
        var response = handler.handleBusinessRule(
                new BusinessRuleException("A solicitação não pode ser alterada."), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().code());
        assertEquals("A solicitação não pode ser alterada.", response.getBody().message());
        assertEquals("/solicitations", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void deveRetornarUnauthorizedComCodigoDeCredenciaisInvalidas() {
        var response = handler.handleInvalidCredentials(new InvalidCredentialsException(), request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getBody().code());
        assertFalse(response.getBody().message().isBlank());
    }

    @Test
    void deveRetornarForbiddenParaAcessoNegado() {
        var response = handler.handleAnalystCoverageDenied(
                new AnalystCoverageDeniedException("A UF da solicitação não está na sua cobertura."), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ACCESS_DENIED", response.getBody().code());
        assertEquals("A UF da solicitação não está na sua cobertura.", response.getBody().message());
    }

    @Test
    void deveRetornarNotFoundParaSolicitacaoInexistente() {
        var response = handler.handleSolicitationNotFound(
                new SolicitationNotFoundException(UUID.randomUUID()), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("SOLICITATION_NOT_FOUND", response.getBody().code());
    }

    @Test
    void deveRetornarBadRequestParaJsonMalformado() {
        var response = handler.handleUnreadableMessage(
                mock(HttpMessageNotReadableException.class), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_REQUEST", response.getBody().code());
        assertFalse(response.getBody().message().isBlank());
    }

    @Test
    void deveRetornarBadRequestParaParametroComTipoInvalido() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                "abc", UUID.class, "id", null, new IllegalArgumentException("invalid UUID"));

        var response = handler.handleTypeMismatch(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_PARAMETER", response.getBody().code());
        assertTrue(response.getBody().message().contains("id"));
        assertTrue(response.getBody().details().containsKey("id"));
    }

    @Test
    void deveOcultarDetalhesInternosEmErroInesperado() {
        var response = handler.handleUnexpected(
                new RuntimeException("password=secret; internal SQL"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertFalse(response.getBody().message().contains("secret"));
        assertFalse(response.getBody().message().contains("SQL"));
    }


    @Test
    void deveRetornarDetalhesDosCamposInvalidos() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Informe um e-mail válido."));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        var response = handler.handleValidation(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals("Informe um e-mail válido.", response.getBody().details().get("email"));
    }

    @Test
    void deveInformarParametroObrigatorioAusente() {
        var exception = new MissingServletRequestParameterException("page", "int");

        var response = handler.handleMissingParameter(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("MISSING_PARAMETER", response.getBody().code());
        assertTrue(response.getBody().details().containsKey("page"));
    }

    @Test
    void deveRetornarConflictQuandoEstadoDeNegocioNaoPermiteOperacao() {
        var response = handler.handleIllegalState(
                new IllegalStateException("A solicitação já foi enviada."), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("BUSINESS_STATE_CONFLICT", response.getBody().code());
        assertEquals("A solicitação já foi enviada.", response.getBody().message());
    }

    @Test
    void deveRetornarBadRequestParaArgumentoInvalido() {
        var response = handler.handleIllegalArgument(
                new IllegalArgumentException("O campo state deve conter duas letras."), request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_ARGUMENT", response.getBody().code());
        assertEquals("O campo state deve conter duas letras.", response.getBody().message());
    }

    @Test
    void deveRetornarServiceUnavailableParaFalhaNaPesquisa() {
        var response = handler.handleElasticsearch(
                new ElasticsearchSearchException("internal detail", new RuntimeException("internal")), request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("SEARCH_SERVICE_UNAVAILABLE", response.getBody().code());
        assertFalse(response.getBody().message().contains("internal"));
    }
}
