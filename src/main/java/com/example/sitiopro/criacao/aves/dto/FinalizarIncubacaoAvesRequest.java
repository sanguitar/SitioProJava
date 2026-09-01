package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.FinalidadeLoteAves;
import com.example.sitiopro.criacao.aves.entity.SexoLoteAves;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class FinalizarIncubacaoAvesRequest {
    @NotNull @Min(0) private Integer pintinhosEclodidos;
    @NotNull @Min(0) private Integer ovosPerdidos;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate dataEclosao;
    @Size(max = 1000) private String observacao;
    private boolean criarLote;
    @Size(max = 120) private String nomeLote;
    private Long instalacaoDestinoId;
    private FinalidadeLoteAves finalidadeLote = FinalidadeLoteAves.MISTA;
    private SexoLoteAves sexoLote = SexoLoteAves.MISTO;
    @Size(max = 100) private String chaveIdempotenciaLote;
    public Integer getPintinhosEclodidos() { return pintinhosEclodidos; } public void setPintinhosEclodidos(Integer v) { pintinhosEclodidos = v; }
    public Integer getOvosPerdidos() { return ovosPerdidos; } public void setOvosPerdidos(Integer v) { ovosPerdidos = v; }
    public LocalDate getDataEclosao() { return dataEclosao; } public void setDataEclosao(LocalDate v) { dataEclosao = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public boolean isCriarLote() { return criarLote; } public void setCriarLote(boolean v) { criarLote = v; }
    public String getNomeLote() { return nomeLote; } public void setNomeLote(String v) { nomeLote = v; }
    public Long getInstalacaoDestinoId() { return instalacaoDestinoId; } public void setInstalacaoDestinoId(Long v) { instalacaoDestinoId = v; }
    public FinalidadeLoteAves getFinalidadeLote() { return finalidadeLote; } public void setFinalidadeLote(FinalidadeLoteAves v) { finalidadeLote = v; }
    public SexoLoteAves getSexoLote() { return sexoLote; } public void setSexoLote(SexoLoteAves v) { sexoLote = v; }
    public String getChaveIdempotenciaLote() { return chaveIdempotenciaLote; } public void setChaveIdempotenciaLote(String v) { chaveIdempotenciaLote = v; }
}
