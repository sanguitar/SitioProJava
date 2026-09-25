package com.example.sitiopro.criacao.suinos.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class ChecagemGestacaoSuinosRequest {
    @NotNull private LocalDate dataChecagem;
    @NotNull private Boolean gestacaoConfirmada;
    @Size(max = 1000) private String observacao;
    public LocalDate getDataChecagem() { return dataChecagem; } public void setDataChecagem(LocalDate v) { dataChecagem = v; }
    public Boolean getGestacaoConfirmada() { return gestacaoConfirmada; } public void setGestacaoConfirmada(Boolean v) { gestacaoConfirmada = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
}
