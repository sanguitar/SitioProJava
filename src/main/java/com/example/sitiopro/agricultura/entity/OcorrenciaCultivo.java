package com.example.sitiopro.agricultura.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "agricultura_ocorrencias")
public class OcorrenciaCultivo extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "cultivo_id", nullable = false) private Cultivo cultivo;
    @Column(name = "data_hora", nullable = false) private LocalDateTime dataHora;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25) private TipoOcorrenciaCultivo tipo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 15) private SeveridadeOcorrencia severidade;
    @Column(nullable = false, length = 180) private String titulo;
    @Column(nullable = false, length = 1000) private String descricao;
    @Column(name = "area_afetada_ha", precision = 14, scale = 4) private BigDecimal areaAfetadaHa;
    @Column(name = "quantidade_perdida", precision = 18, scale = 4) private BigDecimal quantidadePerdida;
    @Column(name = "unidade_perda", length = 30) private String unidadePerda;
    @Column(name = "perda_total", nullable = false) private boolean perdaTotal;
    @Column(name = "chave_idempotencia", nullable = false, length = 80) private String chaveIdempotencia;
    @Column(length = 1000) private String observacao;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 25)
    private StatusOcorrenciaCultivo status = StatusOcorrenciaCultivo.ABERTA;
    @Column(length = 1500) private String resolucao;
    @Column(name = "encerrada_em") private LocalDateTime encerradaEm;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "agricultura_ocorrencia_agrofit_referencias",
            joinColumns = @JoinColumn(name = "ocorrencia_id"),
            inverseJoinColumns = @JoinColumn(name = "agrofit_cultura_id"))
    private Set<AgrofitCultura> referenciasAgrofit = new LinkedHashSet<>();
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; }
    public Cultivo getCultivo() { return cultivo; }
    public void setCultivo(Cultivo cultivo) { this.cultivo = cultivo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public TipoOcorrenciaCultivo getTipo() { return tipo; }
    public void setTipo(TipoOcorrenciaCultivo tipo) { this.tipo = tipo; }
    public SeveridadeOcorrencia getSeveridade() { return severidade; }
    public void setSeveridade(SeveridadeOcorrencia severidade) { this.severidade = severidade; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getAreaAfetadaHa() { return areaAfetadaHa; }
    public void setAreaAfetadaHa(BigDecimal areaAfetadaHa) { this.areaAfetadaHa = areaAfetadaHa; }
    public BigDecimal getQuantidadePerdida() { return quantidadePerdida; }
    public void setQuantidadePerdida(BigDecimal quantidadePerdida) { this.quantidadePerdida = quantidadePerdida; }
    public String getUnidadePerda() { return unidadePerda; }
    public void setUnidadePerda(String unidadePerda) { this.unidadePerda = unidadePerda; }
    public boolean isPerdaTotal() { return perdaTotal; }
    public void setPerdaTotal(boolean perdaTotal) { this.perdaTotal = perdaTotal; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public StatusOcorrenciaCultivo getStatus() { return status; }
    public void setStatus(StatusOcorrenciaCultivo status) { this.status = status; }
    public String getResolucao() { return resolucao; }
    public void setResolucao(String resolucao) { this.resolucao = resolucao; }
    public LocalDateTime getEncerradaEm() { return encerradaEm; }
    public void setEncerradaEm(LocalDateTime encerradaEm) { this.encerradaEm = encerradaEm; }
    public Set<AgrofitCultura> getReferenciasAgrofit() { return Set.copyOf(referenciasAgrofit); }
    public void substituirReferenciasAgrofit(Collection<AgrofitCultura> referencias) {
        referenciasAgrofit.clear();
        referenciasAgrofit.addAll(referencias);
    }
    public long getVersao() { return versao; }
}
