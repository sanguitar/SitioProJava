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
import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.padrao;
import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.servico;

class OpenMeteoSincronizadorCacheTests {

    @Test
    void invalidaClimaSomenteDepoisDaPersistenciaRetornarComSucesso() {
        OpenMeteoClient client = mock(OpenMeteoClient.class);
        OpenMeteoPersistenceService persistence = mock(OpenMeteoPersistenceService.class);
        CacheInvalidationService invalidation = mock(CacheInvalidationService.class);
        OpenMeteoResponse response = mock(OpenMeteoResponse.class);
        ResultadoSincronizacao resultado = new ResultadoSincronizacao(24, 20, 4, 0);
        when(client.buscarPrevisao(padrao())).thenReturn(response);
        when(persistence.persistir(response, padrao())).thenReturn(resultado);
        OpenMeteoSincronizador sincronizador = new OpenMeteoSincronizador(
                client, persistence, new OpenMeteoProperties(), servico(), invalidation);

        assertThat(sincronizador.sincronizar()).isEqualTo(resultado);

        InOrder ordem = inOrder(persistence, invalidation);
        ordem.verify(persistence).persistir(response, padrao());
        ordem.verify(invalidation).invalidarClimaResumo();
    }
}
