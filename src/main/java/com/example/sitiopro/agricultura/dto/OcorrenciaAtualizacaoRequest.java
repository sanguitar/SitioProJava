package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.SeveridadeOcorrencia;
import com.example.sitiopro.agricultura.entity.TipoOcorrenciaCultivo;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OcorrenciaAtualizacaoRequest {
    @NotNull private TipoOcorrenciaCultivo tipo;
    @NotNull private SeveridadeOcorrencia severidade;
    @NotBlank @Size(max = 180) private String titulo;
    @NotBlank @Size(max = 1000) private String descricao;
    @DecimalMin("0.0001") @Digits(integer = 10, fraction = 4) private BigDecimal areaAfetadaHa;
    @Size(max = 1000) private String observacao;
    private List<@Positive Long> agrofitCulturaIds = new ArrayList<>();
    @NotBlank @Size(max = 1500) private String acompanhamento;
    @NotBlank @Size(min = 8, max = 80) @Pattern(regexp = "[A-Za-z0-9._:-]+")
    private String chaveIdempotencia;
    @NotNull @PositiveOrZero private Long versao;

    public TipoOcorrenciaCultivo getTipo() { return tipo; }
    public void setTipo(TipoOcorrenciaCultivo tipo) { this.tipo = tipo; }
    public SeveridadeOcorrencia getSeveridade() { return severidade; }
    public void setSeveridade(SeveridadeOcorrencia severidade) { this.severidade = severidade; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getAreaAfetadaHa() { return areaAfetadaHa; }
    public void setAreaAfetadaHa(BigDecimal areaAfetadaHa) { this.areaAfetadaHa = areaAfetadaHa; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public List<Long> getAgrofitCulturaIds() { return agrofitCulturaIds; }
    public void setAgrofitCulturaIds(List<Long> ids) {
        agrofitCulturaIds = ids == null ? new ArrayList<>() : new ArrayList<>(ids);
    }
    public String getAcompanhamento() { return acompanhamento; }
    public void setAcompanhamento(String acompanhamento) { this.acompanhamento = acompanhamento; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
