package com.example.sitiopro.integracao.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "sitiopro.integracoes.core")
public class IntegracaoCoreProperties {

    private Duration runningTimeout = Duration.ofMinutes(30);
    private int historyLimit = 20;
    private int openMeteoLimitPerMinute = 10;
    private int agrofitLimitPerMinute = 2;

    public Duration getRunningTimeout() {
        return runningTimeout;
    }

    public void setRunningTimeout(Duration runningTimeout) {
        this.runningTimeout = runningTimeout;
    }

    public int getHistoryLimit() {
        return historyLimit;
    }

    public void setHistoryLimit(int historyLimit) {
        this.historyLimit = historyLimit;
    }

    public int getOpenMeteoLimitPerMinute() {
        return openMeteoLimitPerMinute;
    }

    public void setOpenMeteoLimitPerMinute(int openMeteoLimitPerMinute) {
        this.openMeteoLimitPerMinute = openMeteoLimitPerMinute;
    }

    public int getAgrofitLimitPerMinute() {
        return agrofitLimitPerMinute;
    }

    public void setAgrofitLimitPerMinute(int agrofitLimitPerMinute) {
        this.agrofitLimitPerMinute = agrofitLimitPerMinute;
    }
}
