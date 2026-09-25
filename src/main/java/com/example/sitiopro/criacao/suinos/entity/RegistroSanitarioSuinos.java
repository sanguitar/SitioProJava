package com.example.sitiopro.criacao.suinos.entity;

import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "suinos_registros_sanitarios")
public class RegistroSanitarioSuinos extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lote_id") private LoteSuinos lote;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "animal_reprodutivo_id") private AnimalReprodutivoSuinos animalReprodutivo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TipoRegistroSanitarioSuinos tipo;
    @Column(name = "data_procedimento", nullable = false) private LocalDate dataProcedimento;
    @Column(name = "procedimento_produto", nullable = false, length = 200) private String procedimentoProduto;
    @Column(length = 500) private String motivo;
    @Column(name = "responsavel_nome", nullable = false, length = 120) private String responsavel;
    @Column(length = 1000) private String observacao;
    @Column(name = "proxima_acao", length = 250) private String proximaAcao;
    @Column(name = "proxima_acao_data") private LocalDate proximaAcaoData;
    @Column(name = "proxima_acao_concluida", nullable = false) private boolean proximaAcaoConcluida;
    @Column(name = "proxima_acao_concluida_em") private LocalDateTime proximaAcaoConcluidaEm;
    @Column(precision = 19, scale = 2) private BigDecimal custo;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_estoque_id") private ItemEstoque itemEstoque;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "local_estoque_id") private LocalEstoque localEstoque;
    @Column(name = "quantidade_consumida", precision = 19, scale = 4) private BigDecimal quantidadeConsumida;
    @Column(name = "lote_estoque_codigo", length = 100) private String loteEstoqueCodigo;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "movimento_estoque_id") private MovimentoEstoque movimentoEstoque;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; }
    public LoteSuinos getLote() { return lote; } public void setLote(LoteSuinos v) { lote = v; }
    public AnimalReprodutivoSuinos getAnimalReprodutivo() { return animalReprodutivo; } public void setAnimalReprodutivo(AnimalReprodutivoSuinos v) { animalReprodutivo = v; }
    public TipoRegistroSanitarioSuinos getTipo() { return tipo; } public void setTipo(TipoRegistroSanitarioSuinos v) { tipo = v; }
    public LocalDate getDataProcedimento() { return dataProcedimento; } public void setDataProcedimento(LocalDate v) { dataProcedimento = v; }
    public String getProcedimentoProduto() { return procedimentoProduto; } public void setProcedimentoProduto(String v) { procedimentoProduto = v; }
    public String getMotivo() { return motivo; } public void setMotivo(String v) { motivo = v; }
    public String getResponsavel() { return responsavel; } public void setResponsavel(String v) { responsavel = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getProximaAcao() { return proximaAcao; } public void setProximaAcao(String v) { proximaAcao = v; }
    public LocalDate getProximaAcaoData() { return proximaAcaoData; } public void setProximaAcaoData(LocalDate v) { proximaAcaoData = v; }
    public boolean isProximaAcaoConcluida() { return proximaAcaoConcluida; } public void setProximaAcaoConcluida(boolean v) { proximaAcaoConcluida = v; }
    public LocalDateTime getProximaAcaoConcluidaEm() { return proximaAcaoConcluidaEm; } public void setProximaAcaoConcluidaEm(LocalDateTime v) { proximaAcaoConcluidaEm = v; }
    public BigDecimal getCusto() { return custo; } public void setCusto(BigDecimal v) { custo = v; }
    public ItemEstoque getItemEstoque() { return itemEstoque; } public void setItemEstoque(ItemEstoque v) { itemEstoque = v; }
    public LocalEstoque getLocalEstoque() { return localEstoque; } public void setLocalEstoque(LocalEstoque v) { localEstoque = v; }
    public BigDecimal getQuantidadeConsumida() { return quantidadeConsumida; } public void setQuantidadeConsumida(BigDecimal v) { quantidadeConsumida = v; }
    public String getLoteEstoqueCodigo() { return loteEstoqueCodigo; } public void setLoteEstoqueCodigo(String v) { loteEstoqueCodigo = v; }
    public MovimentoEstoque getMovimentoEstoque() { return movimentoEstoque; } public void setMovimentoEstoque(MovimentoEstoque v) { movimentoEstoque = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public long getVersao() { return versao; }
}
