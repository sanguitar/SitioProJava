package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

public class SafraRequest {
    @NotBlank @Size(max = 120)
    private String nome;
    @NotNull @Min(1900) @Max(9999)
    private Integer anoInicio;
    @NotNull @Min(1900) @Max(9999)
    private Integer anoFim;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFim;
    @NotNull
    private StatusSafra status = StatusSafra.PLANEJADA;
    @Size(max = 1000)
    private String observacao;
    @PositiveOrZero
    private Long versao;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getAnoInicio() { return anoInicio; }
    public void setAnoInicio(Integer anoInicio) { this.anoInicio = anoInicio; }
    public Integer getAnoFim() { return anoFim; }
    public void setAnoFim(Integer anoFim) { this.anoFim = anoFim; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public StatusSafra getStatus() { return status; }
    public void setStatus(StatusSafra status) { this.status = status; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
