package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class AcompanhamentoRequest {
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd\'T\'HH:mm")
    private LocalDateTime dataHora;
    @NotNull
    private TipoAcompanhamentoCultivo tipo = TipoAcompanhamentoCultivo.GERAL;
    @NotBlank @Size(max = 1000)
    private String descricao;
    @Size(max = 1000)
    private String observacao;
    @NotNull @PositiveOrZero
    private Long versao;

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public TipoAcompanhamentoCultivo getTipo() { return tipo; }
    public void setTipo(TipoAcompanhamentoCultivo tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
