package com.example.sitiopro.propriedade.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "propriedade_recursos_hidricos")
public class RecursoHidrico extends CadastroFisico {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "area_id")
    private AreaPropriedade area;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private TipoRecursoHidrico tipo;

    @Column(precision = 18, scale = 3)
    private BigDecimal capacidadeLitros;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "volume_atual_litros", precision = 18, scale = 3)
    private BigDecimal volumeAtualLitros;

    @Column(name = "nivel_minimo_alerta_litros", precision = 18, scale = 3)
    private BigDecimal nivelMinimoAlertaLitros;

    @Column(name = "intervalo_inspecao_dias", nullable = false)
    private int intervaloInspecaoDias = 7;

    @Column(name = "ultima_inspecao")
    private LocalDateTime ultimaInspecao;

    @Column(name = "proxima_inspecao")
    private LocalDateTime proximaInspecao;

    public AreaPropriedade getArea() { return area; }
    public void setArea(AreaPropriedade area) { this.area = area; }
    public TipoRecursoHidrico getTipo() { return tipo; }
    public void setTipo(TipoRecursoHidrico tipo) { this.tipo = tipo; }
    public BigDecimal getCapacidadeLitros() { return capacidadeLitros; }
    public void setCapacidadeLitros(BigDecimal capacidadeLitros) { this.capacidadeLitros = capacidadeLitros; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public BigDecimal getVolumeAtualLitros() { return volumeAtualLitros; }
    public void setVolumeAtualLitros(BigDecimal volumeAtualLitros) { this.volumeAtualLitros = volumeAtualLitros; }
    public BigDecimal getNivelMinimoAlertaLitros() { return nivelMinimoAlertaLitros; }
    public void setNivelMinimoAlertaLitros(BigDecimal nivelMinimoAlertaLitros) { this.nivelMinimoAlertaLitros = nivelMinimoAlertaLitros; }
    public int getIntervaloInspecaoDias() { return intervaloInspecaoDias; }
    public void setIntervaloInspecaoDias(int intervaloInspecaoDias) { this.intervaloInspecaoDias = intervaloInspecaoDias; }
    public LocalDateTime getUltimaInspecao() { return ultimaInspecao; }
    public void setUltimaInspecao(LocalDateTime ultimaInspecao) { this.ultimaInspecao = ultimaInspecao; }
    public LocalDateTime getProximaInspecao() { return proximaInspecao; }
    public void setProximaInspecao(LocalDateTime proximaInspecao) { this.proximaInspecao = proximaInspecao; }
}
