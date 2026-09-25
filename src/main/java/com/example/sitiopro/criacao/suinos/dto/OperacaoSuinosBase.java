package com.example.sitiopro.criacao.suinos.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public abstract class OperacaoSuinosBase {
    @NotNull private LocalDateTime dataEvento;
    @NotBlank @Size(max=100) private String chaveIdempotencia;
    @Size(max=1000) private String observacao;
    public LocalDateTime getDataEvento(){return dataEvento;} public void setDataEvento(LocalDateTime v){dataEvento=v;}
    public String getChaveIdempotencia(){return chaveIdempotencia;} public void setChaveIdempotencia(String v){chaveIdempotencia=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
}
