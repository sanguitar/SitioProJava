package com.example.sitiopro.frota.service;

import org.springframework.http.HttpStatus;

public class FrotaOperacaoException extends RuntimeException {
    private final String codigo;
    private final HttpStatus status;

    public FrotaOperacaoException(String codigo, String mensagem) {
        this(codigo, mensagem, HttpStatus.BAD_REQUEST);
    }

    public FrotaOperacaoException(String codigo, String mensagem, HttpStatus status) {
        super(mensagem);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() { return codigo; }
    public HttpStatus getStatus() { return status; }
}
