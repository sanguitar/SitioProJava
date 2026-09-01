package com.example.sitiopro.criacao.aves.dto;

import com.example.sitiopro.criacao.aves.entity.EspecieAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import com.example.sitiopro.criacao.aves.entity.StatusIncubacaoAves;
import com.example.sitiopro.tarefas.dto.AlertaResumo;
import com.example.sitiopro.tarefas.dto.TarefaResumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record IncubacaoAvesDetalhe(
        Long id,
        String codigo,
        MetodoIncubacaoAves metodo,
        EspecieAves especie,
        Long instalacaoId,
        String instalacaoNome,
        LocalDate dataInicio,
        int quantidadeOvos,
        String origemOvos,
        Long loteReprodutorId,
        String loteReprodutorCodigo,
        Long posturaOrigemId,
        LocalDate posturaOrigemData,
        LocalDate dataPrevistaEclosao,
        StatusIncubacaoAves status,
        String observacao,
        String motivoAjustePrevisao,
        Integer pintinhosEclodidos,
        Integer ovosPerdidos,
        LocalDate dataEclosao,
        String observacaoFinalizacao,
        BigDecimal taxaEclosao,
        BigDecimal taxaPerdas,
        Long loteResultanteId,
        String loteResultanteCodigo,
        long periodoPrevistoDias,
        long diasDecorridos,
        long diasRestantes,
        long diaAtual,
        int progressoPercentual,
        BigDecimal ultimaTemperatura,
        BigDecimal ultimaUmidade,
        LocalDateTime ultimaMedicaoEm,
        List<AcompanhamentoIncubacaoAvesResumo> acompanhamentos,
        List<TarefaResumo> tarefas,
        List<AlertaResumo> alertas,
        long versao,
        LocalDateTime criadoEm,
        String criadoPor,
        LocalDateTime alteradoEm,
        String alteradoPor) {
}
