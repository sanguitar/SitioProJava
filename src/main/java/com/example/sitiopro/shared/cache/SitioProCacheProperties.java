package com.example.sitiopro.shared.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("sitiopro.cache")
public class SitioProCacheProperties {

    private Duration climaResumoTtl = Duration.ofMinutes(5);
    private Duration integracoesStatusTtl = Duration.ofSeconds(30);
    private Duration agrofitCulturasTtl = Duration.ofHours(12);
    private final Redis redis = new Redis();

    public Duration getClimaResumoTtl() {
        return climaResumoTtl;
    }

    public void setClimaResumoTtl(Duration climaResumoTtl) {
        this.climaResumoTtl = climaResumoTtl;
    }

    public Duration getIntegracoesStatusTtl() {
        return integracoesStatusTtl;
    }

    public void setIntegracoesStatusTtl(Duration integracoesStatusTtl) {
        this.integracoesStatusTtl = integracoesStatusTtl;
    }

    public Duration getAgrofitCulturasTtl() {
        return agrofitCulturasTtl;
    }

    public void setAgrofitCulturasTtl(Duration agrofitCulturasTtl) {
        this.agrofitCulturasTtl = agrofitCulturasTtl;
    }

    public Redis getRedis() {
        return redis;
    }

    public static class Redis {

        private boolean enabled;
        private String host = "localhost";
        private int port = 6379;
        private String username;
        private String password;
        private Duration connectTimeout = Duration.ofMillis(500);
        private Duration commandTimeout = Duration.ofSeconds(1);
        private Duration retryAfter = Duration.ofSeconds(10);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getCommandTimeout() {
            return commandTimeout;
        }

        public void setCommandTimeout(Duration commandTimeout) {
            this.commandTimeout = commandTimeout;
        }

        public Duration getRetryAfter() {
            return retryAfter;
        }

        public void setRetryAfter(Duration retryAfter) {
            this.retryAfter = retryAfter;
        }
    }
}
