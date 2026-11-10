package com.example.sitiopro.sync;

import org.springframework.http.HttpStatus;

public class SyncException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public SyncException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
