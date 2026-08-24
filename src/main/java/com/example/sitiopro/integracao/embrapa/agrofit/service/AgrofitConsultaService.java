package com.example.sitiopro.integracao.embrapa.agrofit.service;

import com.example.sitiopro.integracao.embrapa.agrofit.dto.AgrofitCulturaResumo;
import com.example.sitiopro.integracao.embrapa.agrofit.dto.AgrofitCulturasResumo;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import com.example.sitiopro.shared.cache.CacheNames;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgrofitConsultaService {

    private final AgrofitCulturaRepository repository;

    public AgrofitConsultaService(AgrofitCulturaRepository repository) {
        this.repository = repository;
    }

    @Cacheable(cacheNames = CacheNames.AGROFIT_CULTURAS, key = "'catalogo-completo'")
    @Transactional(readOnly = true)
    public AgrofitCulturasResumo listarCulturas() {
        return new AgrofitCulturasResumo(repository.findAllByOrderByNome().stream()
                .map(AgrofitCulturaResumo::de)
                .toList());
    }
}
