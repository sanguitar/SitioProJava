package com.example.sitiopro.criacao.peixes.dto;
import com.example.sitiopro.criacao.peixes.entity.TipoEventoPeixes;
import jakarta.validation.constraints.*;
public class PerdaPeixesRequest extends OperacaoPeixesBase {
    @NotNull @Positive private Integer quantidade;
    @NotNull private TipoEventoPeixes tipo;
    public Integer getQuantidade(){return quantidade;} public void setQuantidade(Integer v){quantidade=v;}
    public TipoEventoPeixes getTipo(){return tipo;} public void setTipo(TipoEventoPeixes v){tipo=v;}
}
