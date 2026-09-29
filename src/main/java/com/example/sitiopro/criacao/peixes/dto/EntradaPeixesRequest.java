package com.example.sitiopro.criacao.peixes.dto;
import jakarta.validation.constraints.*;
public class EntradaPeixesRequest extends OperacaoPeixesBase {
    @NotNull @Positive private Integer quantidade;
    public Integer getQuantidade(){return quantidade;} public void setQuantidade(Integer v){quantidade=v;}
}
