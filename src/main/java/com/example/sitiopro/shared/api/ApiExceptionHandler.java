package com.example.sitiopro.shared.api;

import com.example.sitiopro.compras.service.ComprasOperacaoException;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.shared.observability.RequestCorrelation;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@RestControllerAdvice(basePackages = {
        "com.example.sitiopro.estoque.api",
        "com.example.sitiopro.compras.api",
        "com.example.sitiopro.integracao.api"
})
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final Pattern SENSITIVE_VALUE_PATTERN = Pattern.compile(
            "(?i)(senha|password|passwd|pwd|secret|token|authorization|cookie|connection\\s*string|connectionString)\\s*[:=]\\s*\\S+");

    @ExceptionHandler(EstoqueOperacaoException.class)
    public ResponseEntity<ApiErrorResponse> estoque(EstoqueOperacaoException ex, HttpServletRequest request) {
        return negocio(ex.getCode(), ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(ComprasOperacaoException.class)
    public ResponseEntity<ApiErrorResponse> compras(ComprasOperacaoException ex, HttpServletRequest request) {
        return negocio(ex.getCode(), ex.getMessage(), ex.getStatus(), request);
    }

    @ExceptionHandler(IntegracaoOperacaoException.class)
    public ResponseEntity<ApiErrorResponse> integracao(IntegracaoOperacaoException ex, HttpServletRequest request) {
        return negocio(ex.getCode(), ex.getMessage(), ex.getStatus(), request);
    }

    private ResponseEntity<ApiErrorResponse> negocio(String code, String message, HttpStatus status,
            HttpServletRequest request) {
        String requestId = RequestCorrelation.currentRequestId();
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "api_business_error",
                "module", moduleForPath(request.getRequestURI()),
                "http.response.status_code", status.value()))) {
            if (status.is5xxServerError()) {
                log.error("Erro de operação da API: {}", code);
            } else {
                log.warn("Operação da API recusada: {}", code);
            }
        }
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(Instant.now(), status.value(), code, message,
                        request.getRequestURI(), requestId));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String requestId = RequestCorrelation.currentRequestId();
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarErroCampo)
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Dados inválidos.";
        }
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "api_validation_error",
                "module", moduleForPath(request.getRequestURI()),
                "http.response.status_code", HttpStatus.BAD_REQUEST.value()))) {
            log.warn("Request inválido na API: {}", request.getRequestURI());
        }
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(), "VALIDACAO_INVALIDA",
                        message, request.getRequestURI(), requestId));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> inesperado(Exception ex, HttpServletRequest request) {
        String requestId = RequestCorrelation.currentRequestId();
        String sanitizedMessage = sanitize(ex.getMessage());
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "api_unexpected_error",
                "module", moduleForPath(request.getRequestURI()),
                "http.response.status_code", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error.type", ex.getClass().getName(),
                "error.message", sanitizedMessage,
                "error.stack_trace", sanitizedStackTrace(ex)))) {
            log.error("Erro inesperado na API. Código: {}", requestId);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERRO_INTERNO",
                        "Não foi possível concluir a operação. Código: " + requestId,
                        request.getRequestURI(), requestId));
    }

    private String formatarErroCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }

    private String moduleForPath(String path) {
        if (path == null) {
            return "api";
        }
        if (path.startsWith("/api/v1/estoque")) {
            return "estoque";
        }
        if (path.startsWith("/api/v1/compras") || path.startsWith("/api/v1/fornecedores")) {
            return "compras";
        }
        if (path.startsWith("/api/v1/clima") || path.startsWith("/api/v1/admin/integracoes")) {
            return "integracao";
        }
        return "api";
    }

    private String sanitizedStackTrace(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        appendSanitizedStackTrace(builder, throwable, false);
        return builder.toString();
    }

    private void appendSanitizedStackTrace(StringBuilder builder, Throwable throwable, boolean causedBy) {
        if (throwable == null) {
            return;
        }
        if (causedBy) {
            builder.append("Caused by: ");
        }
        builder.append(throwable.getClass().getName());
        String message = sanitize(throwable.getMessage());
        if (!message.isBlank()) {
            builder.append(": ").append(message);
        }
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append(System.lineSeparator()).append("\tat ").append(element);
        }
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            builder.append(System.lineSeparator());
            appendSanitizedStackTrace(builder, throwable.getCause(), true);
        }
    }

    private String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "mensagem indisponível";
        }
        return SENSITIVE_VALUE_PATTERN.matcher(value).replaceAll("$1=<redacted>");
    }
}
