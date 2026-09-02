package com.example.sitiopro.administracao.configuracao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("sitiopro.configuracao-operacional.inicial")
public class ConfiguracaoOperacionalInicialProperties {

    private String nomePropriedade = "Sítio Guaratinguetá";
    private String timezone = "Etc/UTC";
    private String latitude = "";
    private String longitude = "";
    private int diasPadraoIncubacao = 21;
    private int antecedenciaAlertaEclosaoDias = 2;

    public String getNomePropriedade() { return nomePropriedade; }
    public void setNomePropriedade(String valor) { nomePropriedade = valor; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String valor) { timezone = valor; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String valor) { latitude = valor; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String valor) { longitude = valor; }
    public int getDiasPadraoIncubacao() { return diasPadraoIncubacao; }
    public void setDiasPadraoIncubacao(int valor) { diasPadraoIncubacao = valor; }
    public int getAntecedenciaAlertaEclosaoDias() { return antecedenciaAlertaEclosaoDias; }
    public void setAntecedenciaAlertaEclosaoDias(int valor) { antecedenciaAlertaEclosaoDias = valor; }
}
