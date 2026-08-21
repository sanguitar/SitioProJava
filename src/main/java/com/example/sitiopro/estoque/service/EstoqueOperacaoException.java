package com.example.sitiopro.estoque.service;

import org.springframework.http.HttpStatus;

public class EstoqueOperacaoException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public EstoqueOperacaoException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public EstoqueOperacaoException(String code, String message, HttpStatus status) {
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
