package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistrarOvoscopiaIncubacaoAvesRequest {
    @NotNull
    private LocalDate dataOvoscopia;

    private LocalDate proximaVerificacao;

    @Size(max = 120)
    private String responsavel;

    @Size(max = 1000)
    private String observacaoGeral;

    @NotBlank
    @Size(max = 100)
    private String chaveIdempotencia;

    @Valid
    @NotEmpty
    private List<ItemOvoscopiaIncubacaoAvesRequest> itens = new ArrayList<>();

    public LocalDate getDataOvoscopia() { return dataOvoscopia; }
    public void setDataOvoscopia(LocalDate dataOvoscopia) { this.dataOvoscopia = dataOvoscopia; }
    public LocalDate getProximaVerificacao() { return proximaVerificacao; }
    public void setProximaVerificacao(LocalDate proximaVerificacao) { this.proximaVerificacao = proximaVerificacao; }
    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }
    public String getObservacaoGeral() { return observacaoGeral; }
    public void setObservacaoGeral(String observacaoGeral) { this.observacaoGeral = observacaoGeral; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public List<ItemOvoscopiaIncubacaoAvesRequest> getItens() { return itens; }
    public void setItens(List<ItemOvoscopiaIncubacaoAvesRequest> itens) { this.itens = itens; }
}

