package com.example.sitiopro.integracao.clima.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClimaResumo(
        boolean disponivel,
        boolean desatualizado,
        BigDecimal temperatura,
        Integer umidadeRelativa,
        BigDecimal chuvaProximas24h,
        BigDecimal velocidadeVento,
        BigDecimal rajadaVento,
        BigDecimal et0,
        BigDecimal umidadeSolo,
        Integer codigoTempo,
        LocalDateTime dataHoraPrevisao,
        LocalDateTime ultimaAtualizacao,
        String timezone,
        String fonte) {

    public static ClimaResumo naoSincronizado() {
        return new ClimaResumo(false, false, null, null, null, null, null, null, null,
                null, null, null, null, "open-meteo");
    }
}
