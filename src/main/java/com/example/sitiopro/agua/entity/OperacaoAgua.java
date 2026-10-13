package com.example.sitiopro.agua.entity;

import com.example.sitiopro.propriedade.entity.RecursoHidrico;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agua_operacoes")
public class OperacaoAgua extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_origem_id", nullable = false)
    private RecursoHidrico recursoOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurso_destino_id")
    private RecursoHidrico recursoDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOperacaoAgua tipo;

    @Column(name = "data_operacao", nullable = false)
    private LocalDateTime dataOperacao;

    @Column(name = "quantidade_litros", precision = 18, scale = 3)
    private BigDecimal quantidadeLitros;

    @Column(name = "volume_origem_antes_litros", precision = 18, scale = 3)
    private BigDecimal volumeOrigemAntesLitros;

    @Column(name = "volume_origem_depois_litros", precision = 18, scale = 3)
    private BigDecimal volumeOrigemDepoisLitros;

    @Column(name = "volume_destino_antes_litros", precision = 18, scale = 3)
    private BigDecimal volumeDestinoAntesLitros;

    @Column(name = "volume_destino_depois_litros", precision = 18, scale = 3)
    private BigDecimal volumeDestinoDepoisLitros;

    @Column(length = 1000)
    private String observacao;

    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;

    @Version @Column(nullable = false)
    private long versao;

    public Long getId() { return id; }
    public RecursoHidrico getRecursoOrigem() { return recursoOrigem; }
    public void setRecursoOrigem(RecursoHidrico recursoOrigem) { this.recursoOrigem = recursoOrigem; }
    public RecursoHidrico getRecursoDestino() { return recursoDestino; }
    public void setRecursoDestino(RecursoHidrico recursoDestino) { this.recursoDestino = recursoDestino; }
    public TipoOperacaoAgua getTipo() { return tipo; }
    public void setTipo(TipoOperacaoAgua tipo) { this.tipo = tipo; }
    public LocalDateTime getDataOperacao() { return dataOperacao; }
    public void setDataOperacao(LocalDateTime dataOperacao) { this.dataOperacao = dataOperacao; }
    public BigDecimal getQuantidadeLitros() { return quantidadeLitros; }
    public void setQuantidadeLitros(BigDecimal quantidadeLitros) { this.quantidadeLitros = quantidadeLitros; }
    public BigDecimal getVolumeOrigemAntesLitros() { return volumeOrigemAntesLitros; }
    public void setVolumeOrigemAntesLitros(BigDecimal valor) { this.volumeOrigemAntesLitros = valor; }
    public BigDecimal getVolumeOrigemDepoisLitros() { return volumeOrigemDepoisLitros; }
    public void setVolumeOrigemDepoisLitros(BigDecimal valor) { this.volumeOrigemDepoisLitros = valor; }
    public BigDecimal getVolumeDestinoAntesLitros() { return volumeDestinoAntesLitros; }
    public void setVolumeDestinoAntesLitros(BigDecimal valor) { this.volumeDestinoAntesLitros = valor; }
    public BigDecimal getVolumeDestinoDepoisLitros() { return volumeDestinoDepoisLitros; }
    public void setVolumeDestinoDepoisLitros(BigDecimal valor) { this.volumeDestinoDepoisLitros = valor; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }
}
