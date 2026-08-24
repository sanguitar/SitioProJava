package com.example.sitiopro.shared.cache;

import com.example.sitiopro.shared.observability.MdcScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

import java.util.Map;

public class FailOpenCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(FailOpenCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        registrar(exception, cache, "get", true);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        registrar(exception, cache, "put", false);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        registrar(exception, cache, "evict", false);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        registrar(exception, cache, "clear", false);
    }

    private void registrar(RuntimeException exception, Cache cache, String operacao, boolean fallbackSql) {
        Throwable causaRaiz = exception;
        while (causaRaiz.getCause() != null && causaRaiz.getCause() != causaRaiz) {
            causaRaiz = causaRaiz.getCause();
        }
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "cache.redis.error",
                "module", "cache",
                "cache.name", cache.getName(),
                "cache.operation", operacao,
                "cache.fallback.sql", fallbackSql,
                "error.type", exception.getClass().getName(),
                "error.root.type", causaRaiz.getClass().getName()))) {
            log.warn("Falha opcional no cache {} durante {}. O fluxo principal foi preservado.",
                    cache.getName(), operacao);
        }
    }
}
