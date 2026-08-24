package com.example.sitiopro.dashboard.dto;

import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.StatusTarefa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardOperacionalResumo(
        NivelAtencao nivelAtencao,
        String mensagemAtencao,
        TarefasResumo tarefas,
        AlertasResumo alertas,
        EstoqueResumo estoque,
        ComprasResumo compras,
        ClimaResumo clima,
        IntegracoesResumo integracoes,
        LocalDateTime geradoEm) {

    public record TarefasResumo(
            long pendentesHoje,
            long vencidas,
            long criticas,
            long emAndamento,
            List<TarefaItem> proximas) {
    }

    public record TarefaItem(
            Long id,
            String titulo,
            StatusTarefa status,
            PrioridadeTarefa prioridade,
            LocalDateTime dataVencimento,
            String responsavel,
            boolean vencida) {
    }

    public record AlertasResumo(
            long ativos,
            long criticos,
            long altaSeveridade,
            List<AlertaItem> principais) {
    }

    public record AlertaItem(
            Long id,
            String titulo,
            SeveridadeAlerta severidade,
            StatusAlerta status,
            ModuloOrigem modulo,
            LocalDateTime detectadoEm) {
    }

    public record EstoqueResumo(
            long itensAbaixoMinimo,
            long lotesVencidos,
            long lotesProximosVencimento,
            List<EstoqueItem> itensCriticos) {
    }

    public record EstoqueItem(
            Long id,
            String nome,
            String unidade,
            BigDecimal saldo,
            BigDecimal estoqueMinimo) {
    }

    public record ComprasResumo(
            long rascunhos,
            long confirmadasNoMes,
            BigDecimal valorConfirmadoNoMes,
            CompraItem ultimaConfirmada,
            List<CompraItem> recentes) {
    }

    public record CompraItem(
            Long id,
            String fornecedor,
            LocalDate dataCompra,
            StatusCompra status,
            BigDecimal total) {
    }

    public record ClimaResumo(
            EstadoClimaDashboard estado,
            BigDecimal temperatura,
            String condicao,
            BigDecimal chuvaProximas24h,
            Integer probabilidadePrecipitacao,
            BigDecimal velocidadeVento,
            LocalDateTime ultimaAtualizacao) {
    }

    public record IntegracoesResumo(
            long implementadas,
            long operacionais,
            long desatualizadas,
            long comFalha,
            long naoConfiguradas,
            List<IntegracaoItem> problemas) {
    }

    public record IntegracaoItem(
            String slug,
            String nome,
            StatusOperacionalIntegracao status,
            LocalDateTime ultimaTentativa) {
    }
}
