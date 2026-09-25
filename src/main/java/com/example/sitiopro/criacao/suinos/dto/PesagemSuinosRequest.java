package com.example.sitiopro.criacao.suinos.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class PesagemSuinosRequest extends OperacaoSuinosBase {
    @NotNull @DecimalMin("0.0001") @Digits(integer=15,fraction=4) private BigDecimal pesoMedio;
    public BigDecimal getPesoMedio(){return pesoMedio;} public void setPesoMedio(BigDecimal v){pesoMedio=v;}
}
