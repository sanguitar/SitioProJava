package com.example.sitiopro.integracao.core;

import com.example.sitiopro.integracao.core.config.IntegracaoCoreProperties;
import com.example.sitiopro.shared.observability.MdcScope;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class IntegrationResilienceExecutor {

    private static final Logger log = LoggerFactory.getLogger(IntegrationResilienceExecutor.class);

    private final Map<FonteIntegracao, Politica> politicas = new EnumMap<>(FonteIntegracao.class);

    public IntegrationResilienceExecutor(IntegracaoCoreProperties properties) {
        politicas.put(FonteIntegracao.OPEN_METEO,
                criarPolitica(FonteIntegracao.OPEN_METEO, 3, Duration.ofMillis(500),
                        properties.getOpenMeteoLimitPerMinute()));
        politicas.put(FonteIntegracao.EMBRAPA_AGROFIT,
                criarPolitica(FonteIntegracao.EMBRAPA_AGROFIT, 2, Duration.ofSeconds(1),
                        properties.getAgrofitLimitPerMinute()));
    }

    public <T> T executar(FonteIntegracao fonte, Supplier<T> chamada) {
        Politica politica = politicas.get(fonte);
        int[] tentativas = {0};
        Supplier<T> contada = () -> {
            tentativas[0]++;
            return chamada.get();
        };
        Supplier<T> protegida = RateLimiter.decorateSupplier(politica.rateLimiter(), contada);
        protegida = CircuitBreaker.decorateSupplier(politica.circuitBreaker(), protegida);
        protegida = Retry.decorateSupplier(politica.retry(), protegida);

        long inicio = System.nanoTime();
        try (MdcScope ignored = MdcScope.with(Map.of(
                "module", "integracao",
                "integration.source", fonte.getSlug()))) {
            T resultado = protegida.get();
            logConclusao(fonte, "integration.http.completed", tentativas[0], inicio, null);
            return resultado;
        } catch (RequestNotPermitted ex) {
            IntegracaoHttpException falha = new IntegracaoHttpException(
                    "RATE_LIMIT_LOCAL", "Limite local de chamadas atingido.", null,
                    IntegracaoHttpException.Tipo.RATE_LIMIT, null, ex);
            logConclusao(fonte, "integration.http.rate_limited", tentativas[0], inicio, falha);
            throw falha;
        } catch (CallNotPermittedException ex) {
            IntegracaoHttpException falha = new IntegracaoHttpException(
                    "CIRCUIT_BREAKER_ABERTO", "Integração temporariamente protegida pelo circuit breaker.", null,
                    IntegracaoHttpException.Tipo.TRANSIENTE, null, ex);
            logConclusao(fonte, "integration.http.circuit_open", tentativas[0], inicio, falha);
            throw falha;
        } catch (IntegracaoHttpException ex) {
            String evento = switch (ex.getTipo()) {
                case TIMEOUT -> "integration.http.timeout";
                case RATE_LIMIT -> "integration.http.rate_limited";
                default -> "integration.http.failed";
            };
            logConclusao(fonte, evento, tentativas[0], inicio, ex);
            throw ex;
        }
    }

    private Politica criarPolitica(FonteIntegracao fonte, int maxTentativas, Duration esperaRetry,
            int limitePorMinuto) {
        CircuitBreakerConfig circuitConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMinutes(2))
                .recordException(this::deveContabilizarFalha)
                .build();
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(maxTentativas)
                .waitDuration(esperaRetry)
                .retryOnException(this::deveRepetir)
                .build();
        RateLimiterConfig rateConfig = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .limitForPeriod(Math.max(1, limitePorMinuto))
                .timeoutDuration(Duration.ZERO)
                .build();
        return new Politica(
                CircuitBreaker.of(fonte.getSlug(), circuitConfig),
                Retry.of(fonte.getSlug(), retryConfig),
                RateLimiter.of(fonte.getSlug(), rateConfig));
    }

    private boolean deveContabilizarFalha(Throwable throwable) {
        return throwable instanceof IntegracaoHttpException ex && ex.retryable();
    }

    private boolean deveRepetir(Throwable throwable) {
        return throwable instanceof IntegracaoHttpException ex && ex.retryable();
    }

    private void logConclusao(FonteIntegracao fonte, String evento, int tentativas, long inicio,
            IntegracaoHttpException falha) {
        Map<String, Object> campos = new LinkedHashMap<>();
        campos.put("event.action", evento);
        campos.put("module", "integracao");
        campos.put("integration.source", fonte.getSlug());
        campos.put("retry.attempts", tentativas);
        campos.put("event.duration", System.nanoTime() - inicio);
        if (falha != null) {
            campos.put("error.code", falha.getCode());
            campos.put("http.response.status_code", falha.getHttpStatus());
            campos.put("retry_after_seconds", falha.getRetryAfterSeconds());
        }
        try (MdcScope ignored = MdcScope.with(campos)) {
            if (falha == null) {
                log.info("Chamada externa concluída para {} em {} tentativa(s)", fonte.getSlug(), tentativas);
            } else {
                log.warn("Chamada externa falhou para {}: {}", fonte.getSlug(), falha.getCode());
            }
        }
    }

    private record Politica(CircuitBreaker circuitBreaker, Retry retry, RateLimiter rateLimiter) {
    }
}
