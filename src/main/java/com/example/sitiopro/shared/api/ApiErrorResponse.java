package com.example.sitiopro.shared.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String requestId,
        List<ApiFieldError> fieldErrors) {

    public ApiErrorResponse(Instant timestamp, int status, String code, String message,
            String path, String requestId) {
        this(timestamp, status, code, message, path, requestId, List.of());
    }

    @JsonProperty("error")
    public String error() {
        return code;
    }
}
