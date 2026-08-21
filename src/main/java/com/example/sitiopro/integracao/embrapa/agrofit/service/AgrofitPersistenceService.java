package com.example.sitiopro.integracao.embrapa.agrofit.service;

import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.integracao.embrapa.agrofit.AgrofitCulturaPayload;
import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgrofitPersistenceService {

    private final AgrofitCulturaRepository repository;
    private final Clock clock;

    public AgrofitPersistenceService(AgrofitCulturaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public ResultadoSincronizacao persistir(List<AgrofitCulturaPayload> payloads) {
        Map<String, AgrofitCultura> existentes = repository.findAllByOrderByNome().stream()
                .collect(Collectors.toMap(
                        AgrofitCultura::getNomeNormalizado,
                        Function.identity(),
                        (primeira, ignorada) -> primeira,
                        LinkedHashMap::new));
        Map<String, String> recebidas = new LinkedHashMap<>();
        payloads.forEach(payload -> {
            String nome = limitar(payload.nome().trim(), 180);
            recebidas.putIfAbsent(normalizar(nome), nome);
        });

        LocalDateTime obtidoEm = LocalDateTime.now(clock);
        List<AgrofitCultura> alteradas = new ArrayList<>();
        int inseridos = 0;
        int atualizados = 0;
        int ignorados = payloads.size() - recebidas.size();
        for (Map.Entry<String, String> recebida : recebidas.entrySet()) {
            AgrofitCultura existente = existentes.get(recebida.getKey());
            if (existente == null) {
                alteradas.add(new AgrofitCultura(recebida.getValue(), recebida.getKey(), obtidoEm));
                inseridos++;
            } else if (!existente.getNome().equals(recebida.getValue())) {
                existente.atualizarNome(recebida.getValue(), obtidoEm);
                alteradas.add(existente);
                atualizados++;
            } else {
                ignorados++;
            }
        }
        if (!alteradas.isEmpty()) {
            repository.saveAll(alteradas);
        }
        return new ResultadoSincronizacao(payloads.size(), inseridos, atualizados, ignorados);
    }

    private String normalizar(String nome) {
        String semAcentos = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return semAcentos.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private String limitar(String valor, int tamanho) {
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }
}
