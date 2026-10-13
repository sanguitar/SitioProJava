package com.example.sitiopro.frota.model;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import com.example.sitiopro.manutencao.entity.AtivoPatrimonial;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "veiculos")
public class Veiculo extends AuditableEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 20)
    private String placa;

    @Column(nullable = false, length = 30)
    private String tipo;

    private String marcaFipe;
    private String modeloFipe;
    private Integer anoModelo;

    @Column(precision = 19, scale = 2)
    private BigDecimal kmAtual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMedidorVeiculo tipoMedidor = TipoMedidorVeiculo.QUILOMETRAGEM;

    @Column(precision = 19, scale = 2)
    private BigDecimal horimetroAtual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ativo_patrimonial_id")
    private AtivoPatrimonial ativoPatrimonial;

    @Column(nullable = false, length = 30)
    private String situacao = "DISPONIVEL";

    @Column(precision = 19, scale = 2)
    private BigDecimal valorFipe;
    private String ultimaConsultaFipe;
    private String icone;

    @Column(nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;

    @Version
    @Column(nullable = false)
    private long versao;

    public Veiculo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarcaFipe() {
        return marcaFipe;
    }

    public void setMarcaFipe(String marcaFipe) {
        this.marcaFipe = marcaFipe;
    }

    public String getModeloFipe() {
        return modeloFipe;
    }

    public void setModeloFipe(String modeloFipe) {
        this.modeloFipe = modeloFipe;
    }

    public Integer getAnoModelo() {
        return anoModelo;
    }

    public void setAnoModelo(Integer anoModelo) {
        this.anoModelo = anoModelo;
    }

    public BigDecimal getKmAtual() {
        return kmAtual;
    }

    public void setKmAtual(BigDecimal kmAtual) {
        this.kmAtual = kmAtual;
    }

    public TipoMedidorVeiculo getTipoMedidor() { return tipoMedidor; }
    public void setTipoMedidor(TipoMedidorVeiculo tipoMedidor) { this.tipoMedidor = tipoMedidor; }
    public BigDecimal getHorimetroAtual() { return horimetroAtual; }
    public void setHorimetroAtual(BigDecimal horimetroAtual) { this.horimetroAtual = horimetroAtual; }
    public AtivoPatrimonial getAtivoPatrimonial() { return ativoPatrimonial; }
    public void setAtivoPatrimonial(AtivoPatrimonial ativoPatrimonial) { this.ativoPatrimonial = ativoPatrimonial; }

    public String getSituacao() {
        return situacao;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public BigDecimal getValorFipe() {
        return valorFipe;
    }

    public void setValorFipe(BigDecimal valorFipe) {
        this.valorFipe = valorFipe;
    }

    public String getUltimaConsultaFipe() {
        return ultimaConsultaFipe;
    }

    public void setUltimaConsultaFipe(String ultimaConsultaFipe) {
        this.ultimaConsultaFipe = ultimaConsultaFipe;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public long getVersao() { return versao; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Veiculo veiculo = (Veiculo) o;
        return id != null && id.equals(veiculo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
