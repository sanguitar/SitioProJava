package com.example.sitiopro.propriedade.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class VerticePerimetroRequest {
    @NotNull @Min(1) @Max(500) private Integer ordem;
    @NotNull @DecimalMin("-90") @DecimalMax("90") @Digits(integer = 3, fraction = 7)
    private BigDecimal latitude;
    @NotNull @DecimalMin("-180") @DecimalMax("180") @Digits(integer = 3, fraction = 7)
    private BigDecimal longitude;
    @Size(max = 120) private String marco;
    @Size(max = 1000) private String observacao;
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public String getMarco() { return marco; }
    public void setMarco(String marco) { this.marco = marco; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
