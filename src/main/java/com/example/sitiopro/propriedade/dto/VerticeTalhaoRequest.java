package com.example.sitiopro.propriedade.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class VerticeTalhaoRequest {
    @NotNull @Min(1) @Max(500)
    private Integer ordem;
    @NotNull @Digits(integer = 3, fraction = 9)
    private BigDecimal latitude;
    @NotNull @Digits(integer = 3, fraction = 9)
    private BigDecimal longitude;
    @Digits(integer = 6, fraction = 2)
    private BigDecimal altitudeGeodesicaM;
    @Size(max = 80)
    private String marco;
    @Size(max = 500)
    private String observacao;

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public BigDecimal getAltitudeGeodesicaM() { return altitudeGeodesicaM; }
    public void setAltitudeGeodesicaM(BigDecimal altitudeGeodesicaM) { this.altitudeGeodesicaM = altitudeGeodesicaM; }
    public String getMarco() { return marco; }
    public void setMarco(String marco) { this.marco = marco; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
