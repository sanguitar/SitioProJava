package com.example.sitiopro.integracao.clima.dto;

import com.example.sitiopro.integracao.clima.entity.PrevisaoClimatica;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PrevisaoClimaticaResponse(
        LocalDateTime dataHoraPrevisao,
        BigDecimal temperatura,
        Integer umidadeRelativa,
        BigDecimal precipitacao,
        Integer probabilidadePrecipitacao,
        BigDecimal velocidadeVento,
        BigDecimal rajadaVento,
        BigDecimal et0,
        BigDecimal umidadeSolo,
        Integer codigoTempo,
        LocalDateTime obtidoEm,
        String fonte) {

    public static PrevisaoClimaticaResponse de(PrevisaoClimatica previsao) {
        return new PrevisaoClimaticaResponse(
                previsao.getDataHoraPrevisao(),
                previsao.getTemperatura(),
                previsao.getUmidadeRelativa(),
                previsao.getPrecipitacao(),
                previsao.getProbabilidadePrecipitacao(),
                previsao.getVelocidadeVento(),
                previsao.getRajadaVento(),
                previsao.getEt0(),
                previsao.getUmidadeSolo(),
                previsao.getCodigoTempo(),
                previsao.getObtidoEm(),
                previsao.getFonte().getSlug());
    }
}
