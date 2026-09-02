package com.example.sitiopro.integracao.clima.openmeteo;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.integracao.clima.service.OpenMeteoPersistenceService;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoSincronizador;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.shared.cache.CacheInvalidationService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;

@Service
public class OpenMeteoSincronizador implements IntegracaoSincronizador {

    private final OpenMeteoClient client;
    private final OpenMeteoPersistenceService persistenceService;
    private final OpenMeteoProperties properties;
    private final ConfiguracaoOperacionalService configuracaoOperacionalService;
    private final CacheInvalidationService cacheInvalidationService;

    public OpenMeteoSincronizador(OpenMeteoClient client,
            OpenMeteoPersistenceService persistenceService, OpenMeteoProperties properties,
            ConfiguracaoOperacionalService configuracaoOperacionalService,
            CacheInvalidationService cacheInvalidationService) {
        this.client = client;
        this.persistenceService = persistenceService;
        this.properties = properties;
        this.configuracaoOperacionalService = configuracaoOperacionalService;
        this.cacheInvalidationService = cacheInvalidationService;
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
        return properties.getContexto() != null && !properties.getContexto().isBlank()
                && configuracaoOperacionalService.obter().localizacaoConfigurada();
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
        var configuracao = configuracaoOperacionalService.obter();
        ResultadoSincronizacao resultado = persistenceService.persistir(client.buscarPrevisao(configuracao), configuracao);
        cacheInvalidationService.invalidarClimaResumo();
        return resultado;
    }
}
