package com.example.sitiopro.propriedade.service;

import org.springframework.http.HttpStatus;

public class PropriedadeOperacaoException extends RuntimeException {
    private final String campo;
    private final HttpStatus status;
    public PropriedadeOperacaoException(String campo, String mensagem) {
        this(campo, mensagem, HttpStatus.BAD_REQUEST);
    }
    public PropriedadeOperacaoException(String campo, String mensagem, HttpStatus status) {
        super(mensagem);
        this.campo = campo;
        this.status = status;
    }
    public String getCampo() { return campo; }
    public HttpStatus getStatus() { return status; }
}
