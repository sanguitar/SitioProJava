package com.example.sitiopro.integracao.embrapa.agrofit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.ZoneId;

@ConfigurationProperties(prefix = "sitiopro.integracoes.embrapa-agrofit")
public class AgrofitProperties {

    private boolean enabled;
    private String baseUrl = "https://api.cnptia.embrapa.br/agrofit/v1";
    private String token = "";
    private String cron = "0 29 3 * * MON";
    private String scheduleZone = "UTC";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private Duration staleAfter = Duration.ofDays(9);
    private int maxPages = 1;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getScheduleZone() {
        return scheduleZone;
    }

    public void setScheduleZone(String scheduleZone) {
        this.scheduleZone = scheduleZone;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Duration getStaleAfter() {
        return staleAfter;
    }

    public void setStaleAfter(Duration staleAfter) {
        this.staleAfter = staleAfter;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
    }

    public boolean configurada() {
        return baseUrl != null && !baseUrl.isBlank()
                && token != null && !token.isBlank()
                && zonaValida();
    }

    public ZoneId zoneId() {
        try {
            return ZoneId.of(scheduleZone);
        } catch (RuntimeException ex) {
            return ZoneId.of("UTC");
        }
    }

    private boolean zonaValida() {
        try {
            ZoneId.of(scheduleZone);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
