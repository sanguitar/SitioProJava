package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class RegistrarPosturaAvesRequest extends OperacaoLoteAvesRequest {
    @NotNull(message = "Data da coleta é obrigatória") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataColeta;
    @NotNull @Min(0) private Integer ovosInteiros = 0;
    @Min(0) private Integer ovosQuebrados = 0;
    @Min(0) private Integer ovosDescartados = 0;
    public LocalDate getDataColeta() { return dataColeta; } public void setDataColeta(LocalDate v) { dataColeta = v; }
    public Integer getOvosInteiros() { return ovosInteiros; } public void setOvosInteiros(Integer v) { ovosInteiros = v; }
    public Integer getOvosQuebrados() { return ovosQuebrados; } public void setOvosQuebrados(Integer v) { ovosQuebrados = v; }
    public Integer getOvosDescartados() { return ovosDescartados; } public void setOvosDescartados(Integer v) { ovosDescartados = v; }
}
