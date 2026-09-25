package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.suinos.entity.CicloReprodutivoSuinos;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.*;
import org.springframework.stereotype.Service;
import java.time.*;

@Service
public class SuinosReproducaoOperacionalService {
    private final TarefaService tarefas;

    public SuinosReproducaoOperacionalService(TarefaService tarefas) { this.tarefas = tarefas; }

    public void garantirTarefas(CicloReprodutivoSuinos ciclo, UsuarioAtor ator) {
        String referencia = referencia(ciclo.getId());
        if (ciclo.getDataChecagem() == null) {
            sincronizar(ciclo, ator, "CHECAGEM", "Checar gestação de " + ciclo.getMatriz().getCodigo(),
                    "Confirmar ou descartar a gestação.", ciclo.getDataPrevistaChecagem(),
                    PrioridadeTarefa.NORMAL, referencia);
        }
        if (ciclo.getStatus() == com.example.sitiopro.criacao.suinos.entity.StatusCicloReprodutivoSuinos.GESTANTE) {
            sincronizar(ciclo, ator, "PARTO", "Acompanhar parto de " + ciclo.getMatriz().getCodigo(),
                    "Preparar instalação e acompanhar o parto previsto.", ciclo.getDataPrevistaParto(),
                    PrioridadeTarefa.ALTA, referencia);
        }
        if (ciclo.getDataPrevistaDesmame() != null && ciclo.getDataDesmame() == null) {
            sincronizar(ciclo, ator, "DESMAME", "Realizar desmame de " + ciclo.getMatriz().getCodigo(),
                    "Conferir leitões, peso e instalação de destino.", ciclo.getDataPrevistaDesmame(),
                    PrioridadeTarefa.NORMAL, referencia);
        }
    }

    private void sincronizar(CicloReprodutivoSuinos ciclo, UsuarioAtor ator, String marco,
            String titulo, String descricao, LocalDate data, PrioridadeTarefa prioridade, String referencia) {
        tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(
                "CRIACAO:SUINOS:REPRODUCAO:" + ciclo.getId() + ":" + marco,
                titulo, descricao, prioridade, LocalDateTime.of(data, LocalTime.of(8, 0)),
                ModuloOrigem.CRIACOES, referencia), ator);
    }

    public static String referencia(Long cicloId) { return "SUINOS:REPRODUCAO:" + cicloId; }
}
