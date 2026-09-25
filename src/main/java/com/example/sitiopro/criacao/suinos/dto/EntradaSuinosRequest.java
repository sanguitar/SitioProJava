package com.example.sitiopro.criacao.suinos.dto;
import jakarta.validation.constraints.*;
public class EntradaSuinosRequest extends OperacaoSuinosBase {
    @NotNull @Min(1) private Integer quantidade;
    public Integer getQuantidade(){return quantidade;} public void setQuantidade(Integer v){quantidade=v;}
}
