package com.example.sitiopro.integracao.clima.service;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.dto.PrevisaoClimaticaResponse;
import com.example.sitiopro.integracao.clima.entity.PrevisaoClimatica;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.shared.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClimaConsultaService {

    private final PrevisaoClimaticaRepository repository;
    private final OpenMeteoProperties properties;
    private final ConfiguracaoOperacionalService configuracaoOperacionalService;
    private final Clock clock;

    public ClimaConsultaService(PrevisaoClimaticaRepository repository,
            OpenMeteoProperties properties, ConfiguracaoOperacionalService configuracaoOperacionalService,
            Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.configuracaoOperacionalService = configuracaoOperacionalService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.CLIMA_RESUMO, key = "@cacheKeyFactory.climaResumo()")
    public ClimaResumo resumo() {
        var configuracao = configuracaoOperacionalService.obter();
        if (!configuracao.localizacaoConfigurada()) return ClimaResumo.naoSincronizado();
        String contexto = configuracao.contextoClima(properties.getContexto());
        LocalDateTime agoraLocal = LocalDateTime.ofInstant(
                clock.instant(), configuracao.zoneId());
        Optional<PrevisaoClimatica> atual = repository
                .findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
                        FonteIntegracao.OPEN_METEO, contexto, agoraLocal);
        if (atual.isEmpty()) {
            atual = repository.findFirstByFonteAndContextoAndDataHoraPrevisaoGreaterThanOrderByDataHoraPrevisao(
                    FonteIntegracao.OPEN_METEO, contexto, agoraLocal);
        }
        Optional<PrevisaoClimatica> ultima = repository.findFirstByFonteAndContextoOrderByObtidoEmDesc(
                FonteIntegracao.OPEN_METEO, contexto);
        if (atual.isEmpty() || ultima.isEmpty()) {
            return ClimaResumo.naoSincronizado();
        }

        BigDecimal chuva24h = repository
                .findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                        FonteIntegracao.OPEN_METEO,
                        contexto,
                        agoraLocal,
                        agoraLocal.plusHours(24))
                .stream()
                .map(PrevisaoClimatica::getPrecipitacao)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        PrevisaoClimatica previsao = atual.get();
        LocalDateTime ultimaAtualizacao = ultima.get().getObtidoEm();
        boolean desatualizado = ultimaAtualizacao.isBefore(
                LocalDateTime.now(clock).minus(properties.getStaleAfter()));
        return new ClimaResumo(
                true,
                desatualizado,
                previsao.getTemperatura(),
                previsao.getUmidadeRelativa(),
                chuva24h,
                previsao.getVelocidadeVento(),
                previsao.getRajadaVento(),
                previsao.getEt0(),
                previsao.getUmidadeSolo(),
                previsao.getCodigoTempo(),
                previsao.getDataHoraPrevisao(),
                ultimaAtualizacao,
                previsao.getTimezone(),
                previsao.getFonte().getSlug());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoClimaticaResponse> previsao(int horas) {
        int horizonte = Math.max(1, Math.min(horas, 384));
        var configuracao = configuracaoOperacionalService.obter();
        if (!configuracao.localizacaoConfigurada()) return List.of();
        LocalDateTime agoraLocal = LocalDateTime.ofInstant(
                clock.instant(), configuracao.zoneId());
        return repository.findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                        FonteIntegracao.OPEN_METEO,
                        configuracao.contextoClima(properties.getContexto()),
                        agoraLocal,
                        agoraLocal.plusHours(horizonte))
                .stream()
                .map(PrevisaoClimaticaResponse::de)
                .toList();
    }
}
