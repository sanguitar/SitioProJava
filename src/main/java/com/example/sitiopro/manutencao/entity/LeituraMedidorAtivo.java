package com.example.sitiopro.manutencao.entity;

import com.example.sitiopro.shared.audit.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "manutencao_leituras_medidores")
public class LeituraMedidorAtivo extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "ativo_id", nullable = false) private AtivoPatrimonial ativoPatrimonial;
    @Column(name = "data_leitura", nullable = false) private LocalDateTime dataLeitura;
    @Column(precision = 19, scale = 2) private BigDecimal horimetro;
    @Column(precision = 19, scale = 2) private BigDecimal quilometragem;
    @Enumerated(EnumType.STRING) @Column(name = "tipo_leitura", nullable = false, length = 30) private TipoLeituraMedidor tipoLeitura;
    @Column(name = "justificativa_ajuste", length = 500) private String justificativaAjuste;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100) private String chaveIdempotencia;
    @Version @Column(nullable = false) private long versao;

    public Long getId() { return id; }
    public AtivoPatrimonial getAtivoPatrimonial() { return ativoPatrimonial; } public void setAtivoPatrimonial(AtivoPatrimonial v) { ativoPatrimonial = v; }
    public LocalDateTime getDataLeitura() { return dataLeitura; } public void setDataLeitura(LocalDateTime v) { dataLeitura = v; }
    public BigDecimal getHorimetro() { return horimetro; } public void setHorimetro(BigDecimal v) { horimetro = v; }
    public BigDecimal getQuilometragem() { return quilometragem; } public void setQuilometragem(BigDecimal v) { quilometragem = v; }
    public TipoLeituraMedidor getTipoLeitura() { return tipoLeitura; } public void setTipoLeitura(TipoLeituraMedidor v) { tipoLeitura = v; }
    public String getJustificativaAjuste() { return justificativaAjuste; } public void setJustificativaAjuste(String v) { justificativaAjuste = v; }
    public String getChaveIdempotencia() { return chaveIdempotencia; } public void setChaveIdempotencia(String v) { chaveIdempotencia = v; }
    public long getVersao() { return versao; }
}
