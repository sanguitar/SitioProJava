package com.example.sitiopro.integracao.embrapa.agrofit;

import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.integracao.embrapa.agrofit.service.AgrofitPersistenceService;
import com.example.sitiopro.shared.cache.CacheInvalidationService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgrofitSincronizadorCacheTests {

    @Test
    void invalidaCatalogoSomenteDepoisDaPersistenciaRetornarComSucesso() {
        AgrofitClient client = mock(AgrofitClient.class);
        AgrofitPersistenceService persistence = mock(AgrofitPersistenceService.class);
        CacheInvalidationService invalidation = mock(CacheInvalidationService.class);
        AgrofitProperties properties = new AgrofitProperties();
        properties.setMaxPages(1);
        List<AgrofitCulturaPayload> payloads = List.of(new AgrofitCulturaPayload("Milho"));
        ResultadoSincronizacao resultado = new ResultadoSincronizacao(1, 1, 0, 0);
        when(client.buscarCulturas(1)).thenReturn(payloads);
        when(persistence.persistir(payloads)).thenReturn(resultado);
        AgrofitSincronizador sincronizador = new AgrofitSincronizador(
                client, persistence, properties, invalidation);

        assertThat(sincronizador.sincronizar()).isEqualTo(resultado);

        InOrder ordem = inOrder(persistence, invalidation);
        ordem.verify(persistence).persistir(payloads);
        ordem.verify(invalidation).invalidarAgrofitCulturas();
    }
}
