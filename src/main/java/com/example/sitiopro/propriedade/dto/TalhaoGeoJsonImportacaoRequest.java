package com.example.sitiopro.propriedade.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TalhaoGeoJsonImportacaoRequest {
    @NotBlank
    @Size(max = 2_000_000)
    private String geoJson;

    @AssertTrue(message = "Confirme explicitamente a substituição das geometrias.")
    private boolean confirmado;

    public String getGeoJson() { return geoJson; }
    public void setGeoJson(String geoJson) { this.geoJson = geoJson; }
    public boolean isConfirmado() { return confirmado; }
    public void setConfirmado(boolean confirmado) { this.confirmado = confirmado; }
}
