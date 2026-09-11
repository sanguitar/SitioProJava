package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agricultura_irrigacoes")
public class IrrigacaoCultivo extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cultivo_id", nullable = false)
    private Cultivo cultivo;
    @Column(name = "data_hora", nullable = false) private LocalDateTime dataHora;
    @Column(name = "duracao_minutos") private Integer duracaoMinutos;
    @Column(name = "volume_litros", precision = 18, scale = 4) private BigDecimal volumeLitros;
    @Column(length = 120) private String metodo;
    @Column(name = "chave_idempotencia", nullable = false, length = 80) private String chaveIdempotencia;
    @Column(length = 1000) private String observacao;

    public Long getId() { return id; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public Integer getDuracaoMinutos() { return duracaoMinutos; }
    public void setDuracaoMinutos(Integer duracaoMinutos) { this.duracaoMinutos = duracaoMinutos; }
    public BigDecimal getVolumeLitros() { return volumeLitros; }
    public void setVolumeLitros(BigDecimal volumeLitros) { this.volumeLitros = volumeLitros; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
