package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.tarefas.dto.TarefaResumo;
import java.util.List;

public record OcorrenciaDetalhe(OcorrenciaResumo resumo, List<OcorrenciaHistoricoResumo> historico,
        List<TarefaResumo> tarefas) {
    public OcorrenciaDetalhe {
        historico = List.copyOf(historico);
        tarefas = List.copyOf(tarefas);
    }
}
