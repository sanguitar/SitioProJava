package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class CriarIncubacaoAvesRequest {
    @NotNull private Long instalacaoId;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataInicio;
    @NotNull @Min(1) private Integer quantidadeOvos;
    @Size(max = 200) private String origemOvos;
    private Long loteReprodutorId;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataPrevistaEclosao;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    public Long getInstalacaoId() { return instalacaoId; } public void setInstalacaoId(Long v) { instalacaoId = v; }
    public LocalDate getDataInicio() { return dataInicio; } public void setDataInicio(LocalDate v) { dataInicio = v; }
    public Integer getQuantidadeOvos() { return quantidadeOvos; } public void setQuantidadeOvos(Integer v) { quantidadeOvos = v; }
    public String getOrigemOvos() { return origemOvos; } public void setOrigemOvos(String v) { origemOvos = v; }
    public Long getLoteReprodutorId() { return loteReprodutorId; } public void setLoteReprodutorId(Long v) { loteReprodutorId = v; }
    public LocalDate getDataPrevistaEclosao() { return dataPrevistaEclosao; } public void setDataPrevistaEclosao(LocalDate v) { dataPrevistaEclosao = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
