package com.example.sitiopro.shared.cache;

import com.example.sitiopro.shared.observability.MdcScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CacheInvalidationService {

    private static final Logger log = LoggerFactory.getLogger(CacheInvalidationService.class);

    @CacheEvict(cacheNames = CacheNames.CLIMA_RESUMO, allEntries = true)
    public void invalidarClimaResumo() {
        registrar(CacheNames.CLIMA_RESUMO);
    }

    @CacheEvict(cacheNames = CacheNames.INTEGRACOES_STATUS, allEntries = true)
    public void invalidarIntegracoesStatus() {
        registrar(CacheNames.INTEGRACOES_STATUS);
    }

    @CacheEvict(cacheNames = CacheNames.AGROFIT_CULTURAS, allEntries = true)
    public void invalidarAgrofitCulturas() {
        registrar(CacheNames.AGROFIT_CULTURAS);
    }

    private void registrar(String cacheName) {
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "cache.invalidate",
                "module", "cache",
                "cache.name", cacheName))) {
            log.info("Invalidação solicitada para o cache {}.", cacheName);
        }
    }
}
