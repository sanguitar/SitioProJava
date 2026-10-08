package com.example.sitiopro.manutencao.entity;

import com.example.sitiopro.propriedade.entity.EstruturaPropriedade;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "patrimonio_ativos")
public class AtivoPatrimonial extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 20) private String codigo;
    @Column(nullable = false, length = 160) private String nome;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TipoAtivoPatrimonial tipo;
    @Column(length = 100) private String marca;
    @Column(length = 100) private String modelo;
    @Column(name = "numero_serie", length = 120) private String numeroSerie;
    @Column(name = "data_aquisicao") private LocalDate dataAquisicao;
    @Column(name = "valor_aquisicao", precision = 19, scale = 2) private BigDecimal valorAquisicao;
    @Column(length = 160) private String localizacao;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "estrutura_id") private EstruturaPropriedade estrutura;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private StatusAtivoPatrimonial status;
    @Column(length = 1000) private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; } public void setCodigo(String v) { codigo = v; }
    public String getNome() { return nome; } public void setNome(String v) { nome = v; }
    public TipoAtivoPatrimonial getTipo() { return tipo; } public void setTipo(TipoAtivoPatrimonial v) { tipo = v; }
    public String getMarca() { return marca; } public void setMarca(String v) { marca = v; }
    public String getModelo() { return modelo; } public void setModelo(String v) { modelo = v; }
    public String getNumeroSerie() { return numeroSerie; } public void setNumeroSerie(String v) { numeroSerie = v; }
    public LocalDate getDataAquisicao() { return dataAquisicao; } public void setDataAquisicao(LocalDate v) { dataAquisicao = v; }
    public BigDecimal getValorAquisicao() { return valorAquisicao; } public void setValorAquisicao(BigDecimal v) { valorAquisicao = v; }
    public String getLocalizacao() { return localizacao; } public void setLocalizacao(String v) { localizacao = v; }
    public EstruturaPropriedade getEstrutura() { return estrutura; } public void setEstrutura(EstruturaPropriedade v) { estrutura = v; }
    public StatusAtivoPatrimonial getStatus() { return status; } public void setStatus(StatusAtivoPatrimonial v) { status = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public long getVersao() { return versao; }
}
