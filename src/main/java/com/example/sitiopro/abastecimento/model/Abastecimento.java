package com.example.sitiopro.abastecimento.model;

import com.example.sitiopro.frota.model.Veiculo;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "abastecimentos")
public class Abastecimento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    private LocalDate data;

    @Column(precision = 19, scale = 3)
    private BigDecimal litros;

    @Column(precision = 19, scale = 4)
    private BigDecimal precoPorLitro;

    @Column(precision = 19, scale = 2)
    private BigDecimal valorTotal;

    @Column(precision = 19, scale = 2)
    private BigDecimal kmNoAto;

    @Column(precision = 19, scale = 2)
    private BigDecimal horimetroNoAto;

    private String local;

    @Column(nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;

    @Version
    @Column(nullable = false)
    private long versao;

    public Abastecimento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getLitros() {
        return litros;
    }

    public void setLitros(BigDecimal litros) {
        this.litros = litros;
    }

    public BigDecimal getPrecoPorLitro() {
        return precoPorLitro;
    }

    public void setPrecoPorLitro(BigDecimal precoPorLitro) {
        this.precoPorLitro = precoPorLitro;
    }

    public BigDecimal getKmNoAto() {
        return kmNoAto;
    }

    public void setKmNoAto(BigDecimal kmNoAto) {
        this.kmNoAto = kmNoAto;
    }

    public BigDecimal getHorimetroNoAto() { return horimetroNoAto; }
    public void setHorimetroNoAto(BigDecimal horimetroNoAto) { this.horimetroNoAto = horimetroNoAto; }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if (litros != null && precoPorLitro != null) {
            valorTotal = litros.multiply(precoPorLitro).setScale(2, java.math.RoundingMode.HALF_UP);
        }
    }
}
