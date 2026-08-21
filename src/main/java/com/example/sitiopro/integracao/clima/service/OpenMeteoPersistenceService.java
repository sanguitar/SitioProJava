package com.example.sitiopro.integracao.clima.service;

import com.example.sitiopro.integracao.clima.entity.PrevisaoClimatica;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoProperties;
import com.example.sitiopro.integracao.clima.openmeteo.OpenMeteoResponse;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenMeteoPersistenceService {

    private final PrevisaoClimaticaRepository repository;
    private final OpenMeteoProperties properties;
    private final Clock clock;

    public OpenMeteoPersistenceService(PrevisaoClimaticaRepository repository,
            OpenMeteoProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public ResultadoSincronizacao persistir(OpenMeteoResponse response) {
        response.validar();
        OpenMeteoResponse.Hourly hourly = response.hourly();
        LocalDateTime inicio = hourly.time().getFirst();
        LocalDateTime fim = hourly.time().getLast();
        Map<LocalDateTime, PrevisaoClimatica> existentes = new HashMap<>();
        repository.findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
                        FonteIntegracao.OPEN_METEO, properties.getContexto(), inicio, fim)
                .forEach(item -> existentes.put(item.getDataHoraPrevisao(), item));

        LocalDateTime obtidoEm = LocalDateTime.now(clock);
        List<PrevisaoClimatica> alteradas = new ArrayList<>();
        int inseridos = 0;
        int atualizados = 0;
        int ignorados = 0;
        for (int indice = 0; indice < hourly.time().size(); indice++) {
            LocalDateTime dataHora = hourly.time().get(indice);
            PrevisaoClimatica previsao = existentes.get(dataHora);
            boolean nova = previsao == null;
            if (nova) {
                previsao = new PrevisaoClimatica(
                        FonteIntegracao.OPEN_METEO,
                        properties.getContexto(),
                        properties.getTimezone(),
                        dataHora);
            }
            boolean mudou = previsao.atualizar(
                    hourly.temperature2m().get(indice),
                    hourly.relativeHumidity2m().get(indice),
                    hourly.precipitation().get(indice),
                    hourly.precipitationProbability().get(indice),
                    hourly.windSpeed10m().get(indice),
                    hourly.windGusts10m().get(indice),
                    hourly.et0FaoEvapotranspiration().get(indice),
                    hourly.soilMoisture0To1cm().get(indice),
                    hourly.weatherCode().get(indice),
                    obtidoEm,
                    properties.getTimezone());
            if (nova) {
                inseridos++;
                alteradas.add(previsao);
            } else if (mudou) {
                atualizados++;
                alteradas.add(previsao);
            } else {
                ignorados++;
                alteradas.add(previsao);
            }
        }
        if (!alteradas.isEmpty()) {
            repository.saveAll(alteradas);
        }
        int retencao = Math.max(1, properties.getRetentionDays());
        repository.deleteByFonteAndContextoAndDataHoraPrevisaoBefore(
                FonteIntegracao.OPEN_METEO,
                properties.getContexto(),
                LocalDateTime.now(clock).minusDays(retencao));
        return new ResultadoSincronizacao(hourly.time().size(), inseridos, atualizados, ignorados);
    }
}
