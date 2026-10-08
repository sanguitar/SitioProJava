package com.example.sitiopro.manutencao.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manutencao_registros")
public class RegistroManutencao extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ativo_id", nullable = false) private AtivoPatrimonial ativo;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "plano_preventivo_id") private PlanoManutencaoPreventiva planoPreventivo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private TipoManutencao tipo;
    @Column(name = "data_manutencao", nullable = false) private LocalDateTime dataManutencao;
    @Column(nullable = false, length = 500) private String descricao;
    @Column(name = "responsavel_nome", nullable = false, length = 120) private String responsavel;
    @Column(precision = 19, scale = 2) private BigDecimal horimetro;
    @Column(precision = 19, scale = 2) private BigDecimal quilometragem;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal custo = BigDecimal.ZERO;
    @Column(name = "proxima_manutencao") private LocalDateTime proximaManutencao;
    @Column(name = "proxima_manutencao_concluida", nullable = false) private boolean proximaManutencaoConcluida;
    @Column(name = "proxima_manutencao_concluida_em") private LocalDateTime proximaManutencaoConcluidaEm;
    @Column(length = 1000) private String observacao;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @OneToMany(mappedBy = "manutencao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC") private List<ConsumoManutencao> consumos = new ArrayList<>();
    @Version @Column(nullable = false) private long versao;

    public void adicionarConsumo(ConsumoManutencao consumo) { consumo.setManutencao(this); consumos.add(consumo); }
    public Long getId() { return id; }
    public AtivoPatrimonial getAtivo() { return ativo; } public void setAtivo(AtivoPatrimonial v) { ativo = v; }
    public PlanoManutencaoPreventiva getPlanoPreventivo() { return planoPreventivo; } public void setPlanoPreventivo(PlanoManutencaoPreventiva v) { planoPreventivo = v; }
    public TipoManutencao getTipo() { return tipo; } public void setTipo(TipoManutencao v) { tipo = v; }
    public LocalDateTime getDataManutencao() { return dataManutencao; } public void setDataManutencao(LocalDateTime v) { dataManutencao = v; }
    public String getDescricao() { return descricao; } public void setDescricao(String v) { descricao = v; }
    public String getResponsavel() { return responsavel; } public void setResponsavel(String v) { responsavel = v; }
    public BigDecimal getHorimetro() { return horimetro; } public void setHorimetro(BigDecimal v) { horimetro = v; }
    public BigDecimal getQuilometragem() { return quilometragem; } public void setQuilometragem(BigDecimal v) { quilometragem = v; }
    public BigDecimal getCusto() { return custo; } public void setCusto(BigDecimal v) { custo = v; }
    public LocalDateTime getProximaManutencao() { return proximaManutencao; } public void setProximaManutencao(LocalDateTime v) { proximaManutencao = v; }
    public boolean isProximaManutencaoConcluida() { return proximaManutencaoConcluida; } public void setProximaManutencaoConcluida(boolean v) { proximaManutencaoConcluida = v; }
    public LocalDateTime getProximaManutencaoConcluidaEm() { return proximaManutencaoConcluidaEm; } public void setProximaManutencaoConcluidaEm(LocalDateTime v) { proximaManutencaoConcluidaEm = v; }
    public String getObservacao() { return observacao; } public void setObservacao(String v) { observacao = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public List<ConsumoManutencao> getConsumos() { return consumos; }
    public long getVersao() { return versao; }
}
