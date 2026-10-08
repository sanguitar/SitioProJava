package com.example.sitiopro.manutencao.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "manutencao_planos_preventivos")
public class PlanoManutencaoPreventiva extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ativo_id", nullable = false) private AtivoPatrimonial ativoPatrimonial;
    @Column(nullable = false, length = 160) private String nome;
    @Column(length = 500) private String descricao;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_periodicidade", nullable = false, length = 30) private TipoPeriodicidadeManutencao tipoPeriodicidade;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal intervalo;
    @Column(name = "data_referencia") private LocalDateTime dataReferencia;
    @Column(name = "proxima_data") private LocalDateTime proximaData;
    @Column(name = "valor_referencia", precision = 19, scale = 2) private BigDecimal valorReferencia;
    @Column(name = "proximo_valor", precision = 19, scale = 2) private BigDecimal proximoValor;
    @Column(nullable = false) private boolean ativo = true;
    @Column(name = "ciclo_atual", nullable = false) private int cicloAtual = 1;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; }
    public AtivoPatrimonial getAtivoPatrimonial() { return ativoPatrimonial; } public void setAtivoPatrimonial(AtivoPatrimonial v) { ativoPatrimonial = v; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public TipoPeriodicidadeManutencao getTipoPeriodicidade() { return tipoPeriodicidade; } public void setTipoPeriodicidade(TipoPeriodicidadeManutencao v) { tipoPeriodicidade = v; }
    public BigDecimal getIntervalo() { return intervalo; } public void setIntervalo(BigDecimal v) { intervalo = v; }
    public LocalDateTime getDataReferencia() { return dataReferencia; } public void setDataReferencia(LocalDateTime v) { dataReferencia = v; }
    public LocalDateTime getProximaData() { return proximaData; } public void setProximaData(LocalDateTime v) { proximaData = v; }
    public BigDecimal getValorReferencia() { return valorReferencia; } public void setValorReferencia(BigDecimal v) { valorReferencia = v; }
    public BigDecimal getProximoValor() { return proximoValor; } public void setProximoValor(BigDecimal v) { proximoValor = v; }
    public boolean isAtivo() { return ativo; } public void setAtivo(boolean v) { ativo = v; }
    public int getCicloAtual() { return cicloAtual; } public void setCicloAtual(int v) { cicloAtual = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public long getVersao() { return versao; }
}
