package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;

public class TarefaFiltro {

    private StatusTarefa status;
    private PrioridadeTarefa prioridade;
    private Long responsavelId;
    private PrazoTarefa prazo = PrazoTarefa.TODAS;
    private int pagina;
    private int tamanho = 20;

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }

    public PrioridadeTarefa getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeTarefa prioridade) {
        this.prioridade = prioridade;
    }

    public Long getResponsavelId() {
        return responsavelId;
    }

    public void setResponsavelId(Long responsavelId) {
        this.responsavelId = responsavelId;
    }

    public PrazoTarefa getPrazo() {
        return prazo;
    }

    public void setPrazo(PrazoTarefa prazo) {
        this.prazo = prazo == null ? PrazoTarefa.TODAS : prazo;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = Math.max(0, pagina);
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = Math.max(1, Math.min(tamanho, 100));
    }
}
