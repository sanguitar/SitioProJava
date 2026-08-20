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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

@Entity
@Table(name = "abastecimentos")
public class Abastecimento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veiculo_id", nullable = false)
    @NotNull(message = "Veículo é obrigatório")
    private Veiculo veiculo;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @Positive(message = "Quantidade de litros deve ser positiva")
    private Double litros;

    @Positive(message = "Preço deve ser positivo")
    private Double precoPorLitro;

    private Double valorTotal;

    @PositiveOrZero(message = "KM não pode ser negativa")
    private Double kmNoAto;

    @NotBlank(message = "Local é obrigatório")
    private String local;

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

    public Double getLitros() {
        return litros;
    }

    public void setLitros(Double litros) {
        this.litros = litros;
    }

    public Double getPrecoPorLitro() {
        return precoPorLitro;
    }

    public void setPrecoPorLitro(Double precoPorLitro) {
        this.precoPorLitro = precoPorLitro;
    }

    public Double getKmNoAto() {
        return kmNoAto;
    }

    public void setKmNoAto(Double kmNoAto) {
        this.kmNoAto = kmNoAto;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if (litros != null && precoPorLitro != null) {
            valorTotal = litros * precoPorLitro;
        }
    }
}
