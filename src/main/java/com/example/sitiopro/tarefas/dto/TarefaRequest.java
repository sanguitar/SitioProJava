package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class TarefaRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 160, message = "Título deve ter no máximo 160 caracteres")
    private String titulo;

    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    private String descricao;

    @NotNull(message = "Prioridade é obrigatória")
    private PrioridadeTarefa prioridade = PrioridadeTarefa.NORMAL;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataVencimento;

    private Long responsavelId;

    @NotNull(message = "Recorrência é obrigatória")
    private TipoRecorrencia recorrencia = TipoRecorrencia.NENHUMA;

    private Integer intervaloDias;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public PrioridadeTarefa getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeTarefa prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDateTime getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDateTime dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public Long getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }

    public TipoRecorrencia getRecorrencia() {
        return recorrencia;
    }

    public void setRecorrencia(TipoRecorrencia recorrencia) {
        this.recorrencia = recorrencia;
    }

    public Integer getIntervaloDias() {
        return intervaloDias;
    }

    public void setIntervaloDias(Integer intervaloDias) {
        this.intervaloDias = intervaloDias;
    }

    @AssertTrue(message = "Informe um intervalo em dias maior que zero apenas para a recorrência por intervalo")
    public boolean isIntervaloValido() {
        if (recorrencia == null) {
            return true;
        }
        if (recorrencia == TipoRecorrencia.INTERVALO_DIAS) {
            return intervaloDias != null && intervaloDias > 0 && intervaloDias <= 365;
        }
        return intervaloDias == null;
    }
}
