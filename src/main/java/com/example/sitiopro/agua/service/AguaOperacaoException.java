package com.example.sitiopro.agua.service;

import org.springframework.http.HttpStatus;

public class AguaOperacaoException extends RuntimeException {
    private final String codigo;
    private final HttpStatus status;

    public AguaOperacaoException(String codigo, String mensagem) {
        this(codigo, mensagem, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public AguaOperacaoException(String codigo, String mensagem, HttpStatus status) {
        super(mensagem);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() { return codigo; }
    public HttpStatus getStatus() { return status; }
}
