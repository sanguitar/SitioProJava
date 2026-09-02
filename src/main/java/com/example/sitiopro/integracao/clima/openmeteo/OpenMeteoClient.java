package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalLeitura;
import com.example.sitiopro.integracao.core.ExternalHttpExceptionMapper;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoHttpException;
import com.example.sitiopro.integracao.core.IntegrationResilienceExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenMeteoClient {

    private static final String CAMPOS_HORARIOS = String.join(",",
            "temperature_2m",
            "relative_humidity_2m",
            "precipitation",
            "precipitation_probability",
            "wind_speed_10m",
            "wind_gusts_10m",
            "et0_fao_evapotranspiration",
            "soil_moisture_0_to_1cm",
            "weather_code");

    private final RestClient restClient;
    private final OpenMeteoProperties properties;
    private final IntegrationResilienceExecutor resilienceExecutor;

    public OpenMeteoClient(@Qualifier("openMeteoRestClient") RestClient restClient,
            OpenMeteoProperties properties, IntegrationResilienceExecutor resilienceExecutor) {
        this.restClient = restClient;
        this.properties = properties;
        this.resilienceExecutor = resilienceExecutor;
    }

    public OpenMeteoResponse buscarPrevisao(ConfiguracaoOperacionalLeitura configuracao) {
        if (!configuracao.localizacaoConfigurada()) {
            throw new IntegracaoHttpException(
                    "OPEN_METEO_NAO_CONFIGURADO", "Localização do Open-Meteo não configurada.", null,
                    IntegracaoHttpException.Tipo.PERMANENTE);
        }
        return resilienceExecutor.executar(FonteIntegracao.OPEN_METEO, () -> executarRequest(configuracao));
    }

    private OpenMeteoResponse executarRequest(ConfiguracaoOperacionalLeitura configuracao) {
        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/forecast")
                            .queryParam("latitude", configuracao.latitude())
                            .queryParam("longitude", configuracao.longitude())
                            .queryParam("timezone", configuracao.timezone())
                            .queryParam("forecast_days", Math.max(1, Math.min(properties.getForecastDays(), 16)))
                            .queryParam("temperature_unit", "celsius")
                            .queryParam("wind_speed_unit", "kmh")
                            .queryParam("precipitation_unit", "mm")
                            .queryParam("hourly", CAMPOS_HORARIOS)
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);
            if (response == null) {
                throw new IntegracaoHttpException(
                        "OPEN_METEO_PAYLOAD_VAZIO", "O Open-Meteo retornou uma resposta vazia.", null,
                        IntegracaoHttpException.Tipo.PERMANENTE);
            }
            response.validar();
            return response;
        } catch (RuntimeException ex) {
            throw ExternalHttpExceptionMapper.mapear(ex);
        }
    }
}
