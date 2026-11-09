package com.example.sitiopro.usuario.mobile;

import org.springframework.http.HttpStatus;

public class MobileAuthException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public MobileAuthException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
