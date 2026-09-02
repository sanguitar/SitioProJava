package com.example.sitiopro.administracao.configuracao;

import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalLeitura;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;

import java.math.BigDecimal;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public final class ConfiguracaoOperacionalTestFixture {
    private ConfiguracaoOperacionalTestFixture() { }

    public static ConfiguracaoOperacionalLeitura padrao() {
        return new ConfiguracaoOperacionalLeitura("Sítio de teste", "UTC", new BigDecimal("-3"),
                new BigDecimal("-60"), 21, 2, 0, null, null);
    }

    public static ConfiguracaoOperacionalService servico() {
        var service = mock(ConfiguracaoOperacionalService.class);
        lenient().when(service.obter()).thenReturn(padrao());
        return service;
    }
}
