package com.example.sitiopro.criacao.peixes.dto;
import jakarta.validation.constraints.NotNull;
public class TransferenciaPeixesRequest extends OperacaoPeixesBase {
    @NotNull private Long instalacaoDestinoId;
    public Long getInstalacaoDestinoId(){return instalacaoDestinoId;} public void setInstalacaoDestinoId(Long v){instalacaoDestinoId=v;}
}
