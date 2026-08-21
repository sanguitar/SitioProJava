package com.example.sitiopro.integracao.core;

import org.springframework.http.HttpStatus;

public class IntegracaoOperacaoException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public IntegracaoOperacaoException(String code, String message) {
        this(code, message, HttpStatus.CONFLICT);
    }

    public IntegracaoOperacaoException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
