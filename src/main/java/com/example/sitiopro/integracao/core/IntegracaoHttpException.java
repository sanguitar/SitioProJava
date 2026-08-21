package com.example.sitiopro.integracao.core;

public class IntegracaoHttpException extends RuntimeException {

    public enum Tipo {
        PERMANENTE,
        TRANSIENTE,
        TIMEOUT,
        RATE_LIMIT
    }

    private final String code;
    private final Integer httpStatus;
    private final Tipo tipo;
    private final Long retryAfterSeconds;

    public IntegracaoHttpException(String code, String message, Integer httpStatus, Tipo tipo) {
        this(code, message, httpStatus, tipo, null, null);
    }

    public IntegracaoHttpException(String code, String message, Integer httpStatus, Tipo tipo,
            Long retryAfterSeconds, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.httpStatus = httpStatus;
        this.tipo = tipo;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getCode() {
        return code;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public boolean retryable() {
        return tipo == Tipo.TRANSIENTE || tipo == Tipo.TIMEOUT;
    }
}
