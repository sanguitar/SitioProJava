package com.example.sitiopro.administracao.configuracao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record ConfiguracaoOperacionalLeitura(
        String nomePropriedade,
        String timezone,
        BigDecimal latitude,
        BigDecimal longitude,
        int diasPadraoIncubacao,
        int antecedenciaAlertaEclosaoDias,
        long revisaoLocalizacao,
        LocalDateTime alteradoEm,
        String alteradoPor,
        long propriedadeVersao) {

    public ConfiguracaoOperacionalLeitura(String nomePropriedade, String timezone, BigDecimal latitude,
            BigDecimal longitude, int diasPadraoIncubacao, int antecedenciaAlertaEclosaoDias,
            long revisaoLocalizacao, LocalDateTime alteradoEm, String alteradoPor) {
        this(nomePropriedade, timezone, latitude, longitude, diasPadraoIncubacao,
                antecedenciaAlertaEclosaoDias, revisaoLocalizacao, alteradoEm, alteradoPor, 0);
    }

    public boolean localizacaoConfigurada() {
        return latitude != null && longitude != null;
    }

    public ZoneId zoneId() {
        return ZoneId.of(timezone);
    }

    public String contextoClima(String contexto) {
        return revisaoLocalizacao == 0 ? contexto : contexto + "-local-" + revisaoLocalizacao;
    }

    public ConfiguracaoOperacionalForm paraFormulario() {
        ConfiguracaoOperacionalForm form = new ConfiguracaoOperacionalForm();
        form.setNomePropriedade(nomePropriedade);
        form.setPropriedadeVersao(propriedadeVersao);
        form.setTimezone(timezone);
        form.setLatitude(latitude);
        form.setLongitude(longitude);
        form.setDiasPadraoIncubacao(diasPadraoIncubacao);
        form.setAntecedenciaAlertaEclosaoDias(antecedenciaAlertaEclosaoDias);
        return form;
    }
}
