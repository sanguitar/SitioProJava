package com.example.sitiopro.tarefas.service;

import org.springframework.http.HttpStatus;

public class TarefaAlertaOperacaoException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public TarefaAlertaOperacaoException(String code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public TarefaAlertaOperacaoException(String code, String message, HttpStatus status) {
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
