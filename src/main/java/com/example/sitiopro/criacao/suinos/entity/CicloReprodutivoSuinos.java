package com.example.sitiopro.criacao.suinos.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "suinos_ciclos_reprodutivos")
public class CicloReprodutivoSuinos extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 80) private String codigo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "matriz_id") private AnimalReprodutivoSuinos matriz;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reprodutor_id") private AnimalReprodutivoSuinos reprodutor;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private MetodoReproducaoSuinos metodo;
    @Column(name = "data_cobertura", nullable = false) private LocalDate dataCobertura;
    @Column(name = "data_prevista_checagem", nullable = false) private LocalDate dataPrevistaChecagem;
    @Column(name = "data_checagem") private LocalDate dataChecagem;
    @Column(name = "data_prevista_parto", nullable = false) private LocalDate dataPrevistaParto;
    @Column(name = "data_parto") private LocalDate dataParto;
    @Column(name = "nascidos_vivos") private Integer nascidosVivos;
    private Integer natimortos;
    @Column(name = "perdas_parto") private Integer perdasParto;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lote_leitoes_id") private LoteSuinos loteLeitoes;
    @Column(name = "data_prevista_desmame") private LocalDate dataPrevistaDesmame;
    @Column(name = "data_desmame") private LocalDate dataDesmame;
    @Column(name = "peso_medio_desmame", precision = 19, scale = 4) private BigDecimal pesoMedioDesmame;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private StatusCicloReprodutivoSuinos status;
    @Column(length = 1000) private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Column(name = "chave_parto", unique = true, length = 100) private String chaveParto;
    @Column(name = "chave_desmame", unique = true, length = 100) private String chaveDesmame;
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; }
    public String getCodigo() { return codigo; } public void setCodigo(String v) { codigo = v; }
    public AnimalReprodutivoSuinos getMatriz() { return matriz; } public void setMatriz(AnimalReprodutivoSuinos v) { matriz = v; }
    public AnimalReprodutivoSuinos getReprodutor() { return reprodutor; } public void setReprodutor(AnimalReprodutivoSuinos v) { reprodutor = v; }
    public MetodoReproducaoSuinos getMetodo() { return metodo; } public void setMetodo(MetodoReproducaoSuinos v) { metodo = v; }
    public LocalDate getDataCobertura() { return dataCobertura; } public void setDataCobertura(LocalDate v) { dataCobertura = v; }
    public LocalDate getDataPrevistaChecagem() { return dataPrevistaChecagem; } public void setDataPrevistaChecagem(LocalDate v) { dataPrevistaChecagem = v; }
    public LocalDate getDataChecagem() { return dataChecagem; } public void setDataChecagem(LocalDate v) { dataChecagem = v; }
    public LocalDate getDataPrevistaParto() { return dataPrevistaParto; } public void setDataPrevistaParto(LocalDate v) { dataPrevistaParto = v; }
    public LocalDate getDataParto() { return dataParto; } public void setDataParto(LocalDate v) { dataParto = v; }
    public Integer getNascidosVivos() { return nascidosVivos; } public void setNascidosVivos(Integer v) { nascidosVivos = v; }
    public Integer getNatimortos() { return natimortos; } public void setNatimortos(Integer v) { natimortos = v; }
    public Integer getPerdasParto() { return perdasParto; } public void setPerdasParto(Integer v) { perdasParto = v; }
    public LoteSuinos getLoteLeitoes() { return loteLeitoes; } public void setLoteLeitoes(LoteSuinos v) { loteLeitoes = v; }
    public LocalDate getDataPrevistaDesmame() { return dataPrevistaDesmame; } public void setDataPrevistaDesmame(LocalDate v) { dataPrevistaDesmame = v; }
    public LocalDate getDataDesmame() { return dataDesmame; } public void setDataDesmame(LocalDate v) { dataDesmame = v; }
    public BigDecimal getPesoMedioDesmame() { return pesoMedioDesmame; } public void setPesoMedioDesmame(BigDecimal v) { pesoMedioDesmame = v; }
    public StatusCicloReprodutivoSuinos getStatus() { return status; } public void setStatus(StatusCicloReprodutivoSuinos v) { status = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public String getChaveParto() { return chaveParto; } public void setChaveParto(String v) { chaveParto = v; }
    public String getChaveDesmame() { return chaveDesmame; } public void setChaveDesmame(String v) { chaveDesmame = v; }
    public long getVersao() { return versao; }
}
