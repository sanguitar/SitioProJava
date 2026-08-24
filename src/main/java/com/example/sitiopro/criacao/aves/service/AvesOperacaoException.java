package com.example.sitiopro.criacao.aves.service;

import org.springframework.http.HttpStatus;

public class AvesOperacaoException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public AvesOperacaoException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public AvesOperacaoException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
