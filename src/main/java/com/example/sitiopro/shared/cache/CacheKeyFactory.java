package com.example.sitiopro.shared.cache;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("cacheKeyFactory")
public class CacheKeyFactory {

    private final OpenMeteoProperties openMeteoProperties;
    private final ConfiguracaoOperacionalService configuracaoOperacionalService;

    public CacheKeyFactory(OpenMeteoProperties openMeteoProperties,
            ConfiguracaoOperacionalService configuracaoOperacionalService) {
        this.openMeteoProperties = openMeteoProperties;
        this.configuracaoOperacionalService = configuracaoOperacionalService;
    }

    public String climaResumo() {
        return configuracaoOperacionalService.obter().contextoClima(normalizar(openMeteoProperties.getContexto()));
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "principal";
        }
        return valor.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]", "-");
    }
}
