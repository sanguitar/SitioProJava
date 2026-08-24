package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.NotNull;

public class TransferirLoteAvesRequest extends OperacaoLoteAvesRequest {
    @NotNull(message = "Instalação de destino é obrigatória") private Long instalacaoDestinoId;
    public Long getInstalacaoDestinoId() { return instalacaoDestinoId; }
    public void setInstalacaoDestinoId(Long v) { instalacaoDestinoId = v; }
}
