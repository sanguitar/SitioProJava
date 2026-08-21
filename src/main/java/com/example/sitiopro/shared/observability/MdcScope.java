package com.example.sitiopro.shared.observability;

import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MdcScope implements AutoCloseable {

    private final Map<String, String> previousValues;

    private MdcScope(Map<String, String> previousValues) {
        this.previousValues = previousValues;
    }

    public static MdcScope with(Map<String, ?> values) {
        Map<String, String> previous = new LinkedHashMap<>();
        values.forEach((key, value) -> {
            previous.put(key, MDC.get(key));
            if (value == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, String.valueOf(value));
            }
        });
        return new MdcScope(previous);
    }

    @Override
    public void close() {
        previousValues.forEach((key, value) -> {
            if (value == null) {
                MDC.remove(key);
            } else {
                MDC.put(key, value);
            }
        });
    }
}
