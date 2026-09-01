package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AjustarPrevisaoIncubacaoAvesRequest {
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPrevistaEclosao;

    @NotBlank
    @Size(max = 500)
    private String motivo;

    public LocalDate getDataPrevistaEclosao() { return dataPrevistaEclosao; }
    public void setDataPrevistaEclosao(LocalDate valor) { dataPrevistaEclosao = valor; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String valor) { motivo = valor; }
}

