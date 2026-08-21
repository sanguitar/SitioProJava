package com.example.sitiopro.integracao.embrapa.agrofit;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoSincronizador;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.integracao.embrapa.agrofit.service.AgrofitPersistenceService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgrofitSincronizador implements IntegracaoSincronizador {

    private final AgrofitClient client;
    private final AgrofitPersistenceService persistenceService;
    private final AgrofitProperties properties;

    public AgrofitSincronizador(AgrofitClient client,
            AgrofitPersistenceService persistenceService, AgrofitProperties properties) {
        this.client = client;
        this.persistenceService = persistenceService;
        this.properties = properties;
    }

    @Override
    public FonteIntegracao fonte() {
        return FonteIntegracao.EMBRAPA_AGROFIT;
    }

    @Override
    public boolean habilitada() {
        return properties.isEnabled();
    }

    @Override
    public boolean configurada() {
        return properties.configurada();
    }

    @Override
    public String cron() {
        return properties.getCron();
    }

    @Override
    public ZoneId zonaAgendamento() {
        return properties.zoneId();
    }

    @Override
    public Duration limiteDesatualizacao() {
        return properties.getStaleAfter();
    }

    @Override
    public boolean usaCredencial() {
        return true;
    }

    @Override
    public ResultadoSincronizacao sincronizar() {
        int limite = Math.max(1, Math.min(properties.getMaxPages(), 20));
        List<AgrofitCulturaPayload> culturas = new ArrayList<>();
        for (int pagina = 1; pagina <= limite; pagina++) {
            List<AgrofitCulturaPayload> recebidas = client.buscarCulturas(pagina);
            culturas.addAll(recebidas);
            if (recebidas.isEmpty()) {
                break;
            }
        }
        return persistenceService.persistir(culturas);
    }
}
