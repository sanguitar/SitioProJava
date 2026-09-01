package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class CriarIncubacaoAvesRequest {
    @NotNull private MetodoIncubacaoAves metodo = MetodoIncubacaoAves.CHOCADEIRA;
    @NotNull private Long instalacaoId;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataInicio;
    @NotNull @Min(1) private Integer quantidadeOvos;
    @NotNull private EspecieAves especie = EspecieAves.GALINHA;
    @Size(max = 200) private String origemOvos;
    private Long loteReprodutorId;
    private Long posturaOrigemId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataPrevistaEclosao;
    @Size(max = 1000) private String observacao;
    @NotBlank @Size(max = 100) private String chaveIdempotencia;
    public MetodoIncubacaoAves getMetodo() { return metodo; } public void setMetodo(MetodoIncubacaoAves v) { metodo = v; }
    public Long getInstalacaoId() { return instalacaoId; } public void setInstalacaoId(Long v) { instalacaoId = v; }
    public LocalDate getDataInicio() { return dataInicio; } public void setDataInicio(LocalDate v) { dataInicio = v; }
    public Integer getQuantidadeOvos() { return quantidadeOvos; } public void setQuantidadeOvos(Integer v) { quantidadeOvos = v; }
    public EspecieAves getEspecie() { return especie; } public void setEspecie(EspecieAves v) { especie = v; }
    public String getOrigemOvos() { return origemOvos; } public void setOrigemOvos(String v) { origemOvos = v; }
    public Long getLoteReprodutorId() { return loteReprodutorId; } public void setLoteReprodutorId(Long v) { loteReprodutorId = v; }
    public Long getPosturaOrigemId() { return posturaOrigemId; } public void setPosturaOrigemId(Long v) { posturaOrigemId = v; }
    public LocalDate getDataPrevistaEclosao() { return dataPrevistaEclosao; } public void setDataPrevistaEclosao(LocalDate v) { dataPrevistaEclosao = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
}
