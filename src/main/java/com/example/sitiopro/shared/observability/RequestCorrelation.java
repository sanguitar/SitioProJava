package com.example.sitiopro.shared.observability;

import org.slf4j.MDC;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.regex.Pattern;

public final class RequestCorrelation {

    public static final String HEADER_NAME = "X-Request-ID";
    public static final String MDC_REQUEST_ID = "request.id";

    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{8,64}");
    private static final SecureRandom RANDOM = new SecureRandom();

    private RequestCorrelation() {
    }

    public static String normalize(String candidate) {
        if (candidate != null && SAFE_REQUEST_ID.matcher(candidate).matches()) {
            return candidate;
        }
        byte[] bytes = new byte[8];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public static String currentRequestId() {
        String requestId = MDC.get(MDC_REQUEST_ID);
        return requestId == null || requestId.isBlank() ? normalize(null) : requestId;
    }
}
