package com.example.sitiopro.criacao.suinos.dto;
import jakarta.validation.constraints.*;
public class TransferenciaSuinosRequest extends OperacaoSuinosBase {
    @NotNull private Long instalacaoDestinoId;
    public Long getInstalacaoDestinoId(){return instalacaoDestinoId;} public void setInstalacaoDestinoId(Long v){instalacaoDestinoId=v;}
}
