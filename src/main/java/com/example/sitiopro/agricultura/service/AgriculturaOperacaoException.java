package com.example.sitiopro.agricultura.service;
import org.springframework.http.HttpStatus;
public class AgriculturaOperacaoException extends RuntimeException {
    private final HttpStatus status;
    public AgriculturaOperacaoException(String message) { this(message, HttpStatus.BAD_REQUEST); }
    public AgriculturaOperacaoException(String message, HttpStatus status) { super(message); this.status = status; }
    public HttpStatus getStatus() { return status; }
}
