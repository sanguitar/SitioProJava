package com.example.sitiopro.shared.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

public class FailOpenCacheManager implements CacheManager {

    private final CacheManager delegate;
    private final RedisAvailabilityGuard guard;
    private final ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();

    public FailOpenCacheManager(CacheManager delegate, Duration retryAfter) {
        this(delegate, retryAfter, System::nanoTime);
    }

    FailOpenCacheManager(CacheManager delegate, Duration retryAfter, LongSupplier nanoTime) {
        this.delegate = delegate;
        this.guard = new RedisAvailabilityGuard(retryAfter, nanoTime);
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = delegate.getCache(name);
        if (cache == null) {
            return null;
        }
        return caches.computeIfAbsent(name, ignored -> new GuardedCache(cache, guard));
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    private static final class GuardedCache implements Cache {

        private final Cache delegate;
        private final RedisAvailabilityGuard guard;
        private final AtomicBoolean clearPending = new AtomicBoolean();
        private final Set<Object> evictionsPending = ConcurrentHashMap.newKeySet();

        private GuardedCache(Cache delegate, RedisAvailabilityGuard guard) {
            this.delegate = delegate;
            this.guard = guard;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            if (!preparar()) {
                return null;
            }
            try {
                return delegate.get(key);
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            if (!preparar()) {
                return null;
            }
            try {
                return delegate.get(key, type);
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            if (!preparar()) {
                try {
                    return valueLoader.call();
                } catch (Exception ex) {
                    throw new ValueRetrievalException(key, valueLoader, ex);
                }
            }
            try {
                return delegate.get(key, valueLoader);
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public void put(Object key, Object value) {
            if (!guard.tentar()) {
                return;
            }
            try {
                delegate.put(key, value);
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public ValueWrapper putIfAbsent(Object key, Object value) {
            if (!guard.tentar()) {
                return null;
            }
            try {
                return delegate.putIfAbsent(key, value);
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public void evict(Object key) {
            evictionsPending.add(key);
            if (!guard.tentar()) {
                return;
            }
            try {
                delegate.evict(key);
                evictionsPending.remove(key);
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public boolean evictIfPresent(Object key) {
            evictionsPending.add(key);
            if (!guard.tentar()) {
                return false;
            }
            try {
                boolean removed = delegate.evictIfPresent(key);
                evictionsPending.remove(key);
                return removed;
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public void clear() {
            clearPending.set(true);
            if (!guard.tentar()) {
                return;
            }
            try {
                delegate.clear();
                clearPending.set(false);
                evictionsPending.clear();
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        @Override
        public boolean invalidate() {
            clearPending.set(true);
            if (!guard.tentar()) {
                return false;
            }
            try {
                boolean invalidated = delegate.invalidate();
                clearPending.set(false);
                evictionsPending.clear();
                return invalidated;
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }

        private boolean preparar() {
            if (!guard.tentar()) {
                return false;
            }
            try {
                if (clearPending.get()) {
                    delegate.clear();
                    clearPending.set(false);
                    evictionsPending.clear();
                } else if (!evictionsPending.isEmpty()) {
                    for (Object key : Set.copyOf(evictionsPending)) {
                        delegate.evict(key);
                        evictionsPending.remove(key);
                    }
                }
                return true;
            } catch (RuntimeException ex) {
                guard.registrarFalha();
                throw ex;
            }
        }
    }

    private static final class RedisAvailabilityGuard {

        private final long retryAfterNanos;
        private final LongSupplier nanoTime;
        private final AtomicLong blockedUntil = new AtomicLong();

        private RedisAvailabilityGuard(Duration retryAfter, LongSupplier nanoTime) {
            if (retryAfter == null || retryAfter.isNegative() || retryAfter.isZero()) {
                throw new IllegalArgumentException("O intervalo de nova tentativa do Redis deve ser positivo.");
            }
            this.retryAfterNanos = retryAfter.toNanos();
            this.nanoTime = nanoTime;
        }

        private boolean tentar() {
            return nanoTime.getAsLong() >= blockedUntil.get();
        }

        private void registrarFalha() {
            blockedUntil.set(nanoTime.getAsLong() + retryAfterNanos);
        }
    }
}
