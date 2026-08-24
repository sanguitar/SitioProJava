package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EncerrarLoteAvesRequest {
    @NotNull(message = "Status final é obrigatório") private StatusLoteAves statusFinal = StatusLoteAves.ENCERRADO;
    @Size(max = 1000) private String observacao;
    public StatusLoteAves getStatusFinal() { return statusFinal; } public void setStatusFinal(StatusLoteAves v) { statusFinal = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
}
