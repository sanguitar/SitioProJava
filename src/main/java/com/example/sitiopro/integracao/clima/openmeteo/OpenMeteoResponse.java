package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.integracao.core.IntegracaoHttpException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(
        String timezone,
        Hourly hourly) {

    public void validar() {
        if (hourly == null || hourly.time() == null || hourly.time().isEmpty()) {
            throw payloadInvalido();
        }
        int tamanho = hourly.time().size();
        validarTamanho(hourly.temperature2m(), tamanho);
        validarTamanho(hourly.relativeHumidity2m(), tamanho);
        validarTamanho(hourly.precipitation(), tamanho);
        validarTamanho(hourly.precipitationProbability(), tamanho);
        validarTamanho(hourly.windSpeed10m(), tamanho);
        validarTamanho(hourly.windGusts10m(), tamanho);
        validarTamanho(hourly.et0FaoEvapotranspiration(), tamanho);
        validarTamanho(hourly.soilMoisture0To1cm(), tamanho);
        validarTamanho(hourly.weatherCode(), tamanho);
    }

    private void validarTamanho(List<?> valores, int esperado) {
        if (valores == null || valores.size() != esperado) {
            throw payloadInvalido();
        }
    }

    private IntegracaoHttpException payloadInvalido() {
        return new IntegracaoHttpException(
                "OPEN_METEO_PAYLOAD_INVALIDO",
                "A resposta do Open-Meteo está incompleta ou inconsistente.",
                null,
                IntegracaoHttpException.Tipo.PERMANENTE);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
            List<LocalDateTime> time,
            @JsonProperty("temperature_2m") List<BigDecimal> temperature2m,
            @JsonProperty("relative_humidity_2m") List<Integer> relativeHumidity2m,
            List<BigDecimal> precipitation,
            @JsonProperty("precipitation_probability") List<Integer> precipitationProbability,
            @JsonProperty("wind_speed_10m") List<BigDecimal> windSpeed10m,
            @JsonProperty("wind_gusts_10m") List<BigDecimal> windGusts10m,
            @JsonProperty("et0_fao_evapotranspiration") List<BigDecimal> et0FaoEvapotranspiration,
            @JsonProperty("soil_moisture_0_to_1cm") List<BigDecimal> soilMoisture0To1cm,
            @JsonProperty("weather_code") List<Integer> weatherCode) {
    }
}
