package com.example.sitiopro.tarefas.dto;

import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;

public class AlertaFiltro {

    private StatusAlerta status;
    private SeveridadeAlerta severidade;
    private ModuloOrigem modulo;
    private boolean somenteAtivos;
    private int pagina;
    private int tamanho = 20;

    public StatusAlerta getStatus() {
        return status;
    }

    public void setStatus(StatusAlerta status) {
        this.status = status;
    }

    public SeveridadeAlerta getSeveridade() {
        return severidade;
    }

    public void setSeveridade(SeveridadeAlerta severidade) {
        this.severidade = severidade;
    }

    public ModuloOrigem getModulo() {
        return modulo;
    }

    public void setModulo(ModuloOrigem modulo) {
        this.modulo = modulo;
    }

    public boolean isSomenteAtivos() {
        return somenteAtivos;
    }

    public void setSomenteAtivos(boolean somenteAtivos) {
        this.somenteAtivos = somenteAtivos;
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
