package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.service.IntegracaoOrquestrador;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OpenMeteoSchedulerTests {

    @Test
    void agendamentoUsaMesmoOrquestradorDaSincronizacaoManual() {
        IntegracaoOrquestrador orquestrador = mock(IntegracaoOrquestrador.class);

        new OpenMeteoScheduler(orquestrador).sincronizar();

        verify(orquestrador).sincronizar(FonteIntegracao.OPEN_METEO);
    }
}
