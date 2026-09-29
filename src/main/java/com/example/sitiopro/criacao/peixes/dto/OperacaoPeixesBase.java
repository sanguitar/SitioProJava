package com.example.sitiopro.criacao.peixes.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public class OperacaoPeixesBase {
    @NotNull private LocalDateTime dataEvento;
    @Size(max=1000) private String observacao;
    @NotBlank @Size(max=100) private String chaveIdempotencia;
    public LocalDateTime getDataEvento(){return dataEvento;} public void setDataEvento(LocalDateTime v){dataEvento=v;}
    public String getObservacao(){return observacao;} public void setObservacao(String v){observacao=v;}
    public String getChaveIdempotencia(){return chaveIdempotencia;} public void setChaveIdempotencia(String v){chaveIdempotencia=v;}
}
