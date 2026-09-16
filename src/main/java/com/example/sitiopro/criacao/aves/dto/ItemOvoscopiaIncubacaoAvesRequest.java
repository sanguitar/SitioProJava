package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.AchadoOvoscopiaAves;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ItemOvoscopiaIncubacaoAvesRequest {
    @NotNull
    @Positive
    private Integer numero;

    @NotNull
    private AchadoOvoscopiaAves achado;

    @Size(max = 1000)
    private String observacao;

    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public AchadoOvoscopiaAves getAchado() { return achado; }
    public void setAchado(AchadoOvoscopiaAves achado) { this.achado = achado; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}

