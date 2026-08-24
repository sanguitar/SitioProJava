package com.example.sitiopro.shared.cache;

import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("cacheKeyFactory")
public class CacheKeyFactory {

    private final OpenMeteoProperties openMeteoProperties;

    public CacheKeyFactory(OpenMeteoProperties openMeteoProperties) {
        this.openMeteoProperties = openMeteoProperties;
    }

    public String climaResumo() {
        return normalizar(openMeteoProperties.getContexto());
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "principal";
        }
        return valor.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
    }
}
