package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OcorrenciaRequest {
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private LocalDateTime dataHora;
    @NotNull private TipoOcorrenciaCultivo tipo;
    @NotNull private SeveridadeOcorrencia severidade;
    @NotBlank @Size(max = 180) private String titulo;
    @NotBlank @Size(max = 1000) private String descricao;
    @DecimalMin("0.0001") @Digits(integer = 10, fraction = 4) private BigDecimal areaAfetadaHa;
    @DecimalMin("0.0001") @Digits(integer = 14, fraction = 4) private BigDecimal quantidadePerdida;
    @Size(max = 30) private String unidadePerda;
    private boolean perdaTotal;
    @Size(max = 1000) private String observacao;
    private List<@Positive Long> agrofitCulturaIds = new ArrayList<>();
    @NotBlank @Size(min = 8, max = 80) @Pattern(regexp = "[A-Za-z0-9._:-]+") private String chaveIdempotencia;
    @NotNull @PositiveOrZero private Long versao;

    public LocalDateTime getDataHora() { return dataHora; } public void setDataHora(LocalDateTime v) { dataHora = v; }
    public TipoOcorrenciaCultivo getTipo() { return tipo; } public void setTipo(TipoOcorrenciaCultivo v) { tipo = v; }
    public SeveridadeOcorrencia getSeveridade() { return severidade; } public void setSeveridade(SeveridadeOcorrencia v) { severidade = v; }
    public String getTitulo() { return titulo; } public void setTitulo(String v) { titulo = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public BigDecimal getAreaAfetadaHa() { return areaAfetadaHa; } public void setAreaAfetadaHa(BigDecimal v) { areaAfetadaHa = v; }
    public BigDecimal getQuantidadePerdida() { return quantidadePerdida; } public void setQuantidadePerdida(BigDecimal v) { quantidadePerdida = v; }
    public String getUnidadePerda() { return unidadePerda; } public void setUnidadePerda(String v) { unidadePerda = v; }
    public boolean isPerdaTotal() { return perdaTotal; } public void setPerdaTotal(boolean v) { perdaTotal = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public List<Long> getAgrofitCulturaIds() { return agrofitCulturaIds; }
    public void setAgrofitCulturaIds(List<Long> v) { agrofitCulturaIds = v == null ? new ArrayList<>() : new ArrayList<>(v); }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public Long getVersao() { return versao; } public void setVersao(Long v) { versao = v; }
}
