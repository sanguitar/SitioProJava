package com.example.sitiopro.propriedade.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class VerticeTalhao {
    @Column(nullable = false)
    private int ordem;
    @Column(nullable = false, precision = 12, scale = 9)
    private BigDecimal latitude;
    @Column(nullable = false, precision = 12, scale = 9)
    private BigDecimal longitude;
    @Column(name = "altitude_geodesica_m", precision = 8, scale = 2)
    private BigDecimal altitudeGeodesicaM;
    @Column(length = 80)
    private String marco;
    @Column(length = 500)
    private String observacao;

    protected VerticeTalhao() {}

    public VerticeTalhao(int ordem, BigDecimal latitude, BigDecimal longitude, BigDecimal altitudeGeodesicaM,
            String marco, String observacao) {
        this.ordem = ordem;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeGeodesicaM = altitudeGeodesicaM;
        this.marco = marco;
        this.observacao = observacao;
    }

    public int getOrdem() { return ordem; }
    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public BigDecimal getAltitudeGeodesicaM() { return altitudeGeodesicaM; }
    public String getMarco() { return marco; }
    public String getObservacao() { return observacao; }
}
