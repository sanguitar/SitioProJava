package com.example.sitiopro.usuario.service;

public class SenhaInvalidaException extends RuntimeException {

    public SenhaInvalidaException(String message) {
        super(message);
    }
}
