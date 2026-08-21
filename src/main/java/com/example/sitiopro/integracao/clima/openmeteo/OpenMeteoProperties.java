package com.example.sitiopro.integracao.clima.openmeteo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;

@ConfigurationProperties(prefix = "sitiopro.integracoes.open-meteo")
public class OpenMeteoProperties {

    private boolean enabled;
    private String baseUrl = "https://api.open-meteo.com";
    private String latitude = "";
    private String longitude = "";
    private String timezone = "";
    private String scheduleZone = "UTC";
    private String contexto = "principal";
    private String cron = "0 17 */3 * * *";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(8);
    private Duration staleAfter = Duration.ofHours(6);
    private int forecastDays = 7;
    private int retentionDays = 30;

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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getContexto() {
        return contexto;
    }

    public String getScheduleZone() {
        return scheduleZone;
    }

    public void setScheduleZone(String scheduleZone) {
        this.scheduleZone = scheduleZone;
    }

    public void setContexto(String contexto) {
        this.contexto = contexto;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
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

    public int getForecastDays() {
        return forecastDays;
    }

    public void setForecastDays(int forecastDays) {
        this.forecastDays = forecastDays;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public boolean configurada() {
        try {
            BigDecimal lat = latitudeDecimal();
            BigDecimal lon = longitudeDecimal();
            ZoneId.of(timezone);
            return lat.compareTo(BigDecimal.valueOf(-90)) >= 0
                    && lat.compareTo(BigDecimal.valueOf(90)) <= 0
                    && lon.compareTo(BigDecimal.valueOf(-180)) >= 0
                    && lon.compareTo(BigDecimal.valueOf(180)) <= 0
                    && contexto != null && !contexto.isBlank();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public BigDecimal latitudeDecimal() {
        return new BigDecimal(latitude == null ? "" : latitude.trim());
    }

    public BigDecimal longitudeDecimal() {
        return new BigDecimal(longitude == null ? "" : longitude.trim());
    }

    public ZoneId zoneId() {
        return configurada() ? ZoneId.of(timezone) : ZoneId.of("UTC");
    }

    public ZoneId scheduleZoneId() {
        try {
            return ZoneId.of(scheduleZone);
        } catch (RuntimeException ex) {
            return ZoneId.of("UTC");
        }
    }
}
