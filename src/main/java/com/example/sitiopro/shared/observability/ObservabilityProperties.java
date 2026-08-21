package com.example.sitiopro.shared.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sitiopro.observability")
public class ObservabilityProperties {

    private String serviceName = "sitioguaratingueta";
    private String environment = "dev";
    private boolean enabled;
    private String apmServerUrl = "http://apm-server:8200";

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApmServerUrl() {
        return apmServerUrl;
    }

    public void setApmServerUrl(String apmServerUrl) {
        this.apmServerUrl = apmServerUrl;
    }
}
