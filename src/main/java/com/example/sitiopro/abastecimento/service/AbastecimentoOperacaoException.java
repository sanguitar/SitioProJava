package com.example.sitiopro.abastecimento.service;

import org.springframework.http.HttpStatus;

public class AbastecimentoOperacaoException extends RuntimeException {
    private final String codigo;
    private final HttpStatus status;

    public AbastecimentoOperacaoException(String codigo, String mensagem) {
        this(codigo, mensagem, HttpStatus.BAD_REQUEST);
    }

    public AbastecimentoOperacaoException(String codigo, String mensagem, HttpStatus status) {
        super(mensagem);
        this.codigo = codigo;
        this.status = status;
    }

    public String getCodigo() { return codigo; }
    public HttpStatus getStatus() { return status; }
}
