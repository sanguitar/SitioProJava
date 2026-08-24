package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.integracao.clima.service.OpenMeteoPersistenceService;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.shared.cache.CacheInvalidationService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OpenMeteoSincronizadorCacheTests {

    @Test
    void invalidaClimaSomenteDepoisDaPersistenciaRetornarComSucesso() {
        OpenMeteoClient client = mock(OpenMeteoClient.class);
        OpenMeteoPersistenceService persistence = mock(OpenMeteoPersistenceService.class);
        CacheInvalidationService invalidation = mock(CacheInvalidationService.class);
        OpenMeteoResponse response = mock(OpenMeteoResponse.class);
        ResultadoSincronizacao resultado = new ResultadoSincronizacao(24, 20, 4, 0);
        when(client.buscarPrevisao()).thenReturn(response);
        when(persistence.persistir(response)).thenReturn(resultado);
        OpenMeteoSincronizador sincronizador = new OpenMeteoSincronizador(
                client, persistence, new OpenMeteoProperties(), invalidation);

        assertThat(sincronizador.sincronizar()).isEqualTo(resultado);

        InOrder ordem = inOrder(persistence, invalidation);
        ordem.verify(persistence).persistir(response);
        ordem.verify(invalidation).invalidarClimaResumo();
    }
}
