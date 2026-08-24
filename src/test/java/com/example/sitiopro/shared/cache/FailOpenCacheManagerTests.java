package com.example.sitiopro.shared.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailOpenCacheManagerTests {

    @Test
    void bloqueiaOperacoesAposFalhaESondaNovamenteSemSleep() {
        AtomicLong tempo = new AtomicLong(1);
        AtomicBoolean falhar = new AtomicBoolean(true);
        Cache delegate = new ConcurrentMapCache("teste") {
            @Override
            public ValueWrapper get(Object key) {
                if (falhar.get()) {
                    throw new IllegalStateException("Redis fora");
                }
                return super.get(key);
            }
        };
        CacheManager manager = new FailOpenCacheManager(
                manager(delegate), Duration.ofSeconds(10), tempo::get);
        Cache cache = manager.getCache("teste");

        assertThatThrownBy(() -> cache.get("chave")).isInstanceOf(IllegalStateException.class);
        falhar.set(false);
        cache.put("chave", "valor");
        assertThat(cache.get("chave")).isNull();

        tempo.addAndGet(Duration.ofSeconds(11).toNanos());
        cache.put("chave", "valor");
        assertThat(cache.get("chave", String.class)).isEqualTo("valor");
    }

    @Test
    void reaplicaInvalidacaoPendenteAntesDaPrimeiraLeituraAposRecuperacao() {
        AtomicLong tempo = new AtomicLong(1);
        AtomicBoolean falhar = new AtomicBoolean(true);
        ConcurrentMapCache delegate = new ConcurrentMapCache("teste");
        delegate.put("antigo", "valor-antigo");
        Cache failing = new ConcurrentMapCache("teste", delegate.getNativeCache(), true) {
            @Override
            public ValueWrapper get(Object key) {
                if (falhar.get()) {
                    throw new IllegalStateException("Redis fora");
                }
                return super.get(key);
            }
        };
        Cache cache = new FailOpenCacheManager(
                manager(failing), Duration.ofSeconds(10), tempo::get).getCache("teste");

        assertThatThrownBy(() -> cache.get("antigo")).isInstanceOf(IllegalStateException.class);
        cache.clear();
        falhar.set(false);
        tempo.addAndGet(Duration.ofSeconds(11).toNanos());

        assertThat(cache.get("antigo")).isNull();
    }

    private CacheManager manager(Cache cache) {
        return new CacheManager() {
            @Override
            public Cache getCache(String name) {
                return cache;
            }

            @Override
            public Collection<String> getCacheNames() {
                return List.of(cache.getName());
            }
        };
    }
}
