package com.example.sitiopro.criacao.aves.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public abstract class OperacaoLoteAvesRequest {
    @NotBlank(message = "Chave de idempotência é obrigatória") @Size(max = 100)
    private String chaveIdempotencia;
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataEvento;
    @Size(max = 1000)
    private String observacao;

    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String valor) { this.chaveIdempotencia = valor; }
    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime valor) { this.dataEvento = valor; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String valor) { this.observacao = valor; }
}
