package com.example.sitiopro.criacao.suinos.dto;
import com.example.sitiopro.criacao.suinos.entity.TipoEventoSuinos;
import jakarta.validation.constraints.*;
public class PerdaSuinosRequest extends OperacaoSuinosBase {
    @NotNull private TipoEventoSuinos tipo;
    @NotNull @Min(1) private Integer quantidade;
    public TipoEventoSuinos getTipo(){return tipo;} public void setTipo(TipoEventoSuinos v){tipo=v;}
    public Integer getQuantidade(){return quantidade;} public void setQuantidade(Integer v){quantidade=v;}
}
