package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.integracao.clima.service.OpenMeteoPersistenceService;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoSincronizador;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;

@Service
public class OpenMeteoSincronizador implements IntegracaoSincronizador {

    private final OpenMeteoClient client;
    private final OpenMeteoPersistenceService persistenceService;
    private final OpenMeteoProperties properties;

    public OpenMeteoSincronizador(OpenMeteoClient client,
            OpenMeteoPersistenceService persistenceService, OpenMeteoProperties properties) {
        this.client = client;
        this.persistenceService = persistenceService;
        this.properties = properties;
    }

    @Override
    public FonteIntegracao fonte() {
        return FonteIntegracao.OPEN_METEO;
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
        return properties.scheduleZoneId();
    }

    @Override
    public Duration limiteDesatualizacao() {
        return properties.getStaleAfter();
    }

    @Override
    public boolean usaCredencial() {
        return false;
    }

    @Override
    public ResultadoSincronizacao sincronizar() {
        return persistenceService.persistir(client.buscarPrevisao());
    }
}
