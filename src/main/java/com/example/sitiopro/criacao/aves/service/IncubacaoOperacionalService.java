package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.config.AvesProperties;
import com.example.sitiopro.criacao.aves.entity.IncubacaoAves;
import com.example.sitiopro.tarefas.dto.AlertaResumo;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.dto.TarefaResumo;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class IncubacaoOperacionalService {

    private final TarefaService tarefaService;
    private final AlertaService alertaService;
    private final AvesProperties properties;

    public IncubacaoOperacionalService(TarefaService tarefaService, AlertaService alertaService,
            AvesProperties properties) {
        this.tarefaService = tarefaService;
        this.alertaService = alertaService;
        this.properties = properties;
    }

    @Transactional
    public void garantirTarefas(IncubacaoAves incubacao, UsuarioAtor ator) {
        if (!properties.isTarefasAutomaticasEnabled()) return;
        String referencia = referencia(incubacao.getId());
        sincronizar(incubacao, ator, "VERIFICACAO_INICIAL", "Verificar " + incubacao.getCodigo(),
                "Conferir a incubação e registrar observações.",
                incubacao.getDataInicio().plusDays(properties.getTarefaVerificacaoDias()),
                PrioridadeTarefa.NORMAL, referencia);
        sincronizar(incubacao, ator, "OVOSCOPIA", "Realizar ovoscopia de " + incubacao.getCodigo(),
                "Registrar quantidade avaliada, ovos férteis e ovos sem desenvolvimento.",
                incubacao.getDataInicio().plusDays(properties.getTarefaOvoscopiaDias()),
                PrioridadeTarefa.NORMAL, referencia);
        LocalDate preparacao = incubacao.getDataPrevistaEclosao()
                .minusDays(properties.getTarefaPreparacaoAntecedenciaDias());
        if (preparacao.isBefore(incubacao.getDataInicio())) preparacao = incubacao.getDataInicio();
        sincronizar(incubacao, ator, "PREPARACAO_ECLOSAO", "Preparar eclosão de " + incubacao.getCodigo(),
                "Conferir o local e preparar o recebimento dos pintinhos.", preparacao,
                PrioridadeTarefa.ALTA, referencia);
        sincronizar(incubacao, ator, "ECLOSAO_PREVISTA", "Conferir eclosão de " + incubacao.getCodigo(),
                "Acompanhar a eclosão prevista e finalizar o ciclo.", incubacao.getDataPrevistaEclosao(),
                PrioridadeTarefa.ALTA, referencia);
    }

    @Transactional(readOnly = true)
    public List<TarefaResumo> tarefas(Long incubacaoId) {
        return tarefaService.listarRelacionadas(ModuloOrigem.CRIACOES, referencia(incubacaoId));
    }

    @Transactional(readOnly = true)
    public List<AlertaResumo> alertas(Long incubacaoId) {
        return alertaService.listarRelacionados(ModuloOrigem.CRIACOES, referencia(incubacaoId));
    }

    private void sincronizar(IncubacaoAves incubacao, UsuarioAtor ator, String marco,
            String titulo, String descricao, LocalDate data, PrioridadeTarefa prioridade, String referencia) {
        tarefaService.sincronizarAutomatica(new TarefaAutomaticaRequest(
                "CRIACAO:AVES:INCUBACAO:" + incubacao.getId() + ":" + marco,
                titulo, descricao, prioridade, LocalDateTime.of(data, LocalTime.of(8, 0)),
                ModuloOrigem.CRIACOES, referencia), ator);
    }

    private String referencia(Long incubacaoId) {
        return "INCUBACAO:" + incubacaoId;
    }
}
