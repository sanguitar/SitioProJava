package com.example.sitiopro.dashboard.service;

import com.example.sitiopro.compras.dto.CompraResumo;
import com.example.sitiopro.compras.dto.ComprasDashboardResumo;
import com.example.sitiopro.compras.service.CompraService;
import com.example.sitiopro.dashboard.dto.DashboardOperacionalResumo;
import com.example.sitiopro.dashboard.dto.EstadoClimaDashboard;
import com.example.sitiopro.dashboard.dto.NivelAtencao;
import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.dto.LoteEstoqueResumo;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.dto.PrevisaoClimaticaResponse;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;
import com.example.sitiopro.integracao.core.dto.IntegracaoFonteResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.dto.TarefasAlertasPainelResumo;
import com.example.sitiopro.tarefas.service.ResumoOperacionalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);
    private static final int LIMITE_DESTAQUES = 5;
    private static final int DIAS_LOTE_PROXIMO = 30;

    private final ResumoOperacionalService tarefasAlertasService;
    private final EstoqueMovimentoService estoqueService;
    private final CompraService compraService;
    private final ClimaConsultaService climaService;
    private final IntegracaoPainelService integracaoService;
    private final DashboardTendenciasService tendenciasService;
    private final Clock clock;

    public DashboardService(ResumoOperacionalService tarefasAlertasService,
            EstoqueMovimentoService estoqueService,
            CompraService compraService,
            ClimaConsultaService climaService,
            IntegracaoPainelService integracaoService,
            DashboardTendenciasService tendenciasService,
            Clock clock) {
        this.tarefasAlertasService = tarefasAlertasService;
        this.estoqueService = estoqueService;
        this.compraService = compraService;
        this.climaService = climaService;
        this.integracaoService = integracaoService;
        this.tendenciasService = tendenciasService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardOperacionalResumo montarResumo() {
        long inicio = System.nanoTime();
        TarefasAlertasPainelResumo operacao = tarefasAlertasService.resumoPainel(LIMITE_DESTAQUES);
        DashboardOperacionalResumo.TarefasResumo tarefas = tarefas(operacao.tarefas());
        DashboardOperacionalResumo.AlertasResumo alertas = alertas(operacao.alertas());
        DashboardOperacionalResumo.EstoqueResumo estoque = estoque();
        DashboardOperacionalResumo.ComprasResumo compras = compras(compraService.montarResumo());
        DashboardOperacionalResumo.ClimaResumo clima = clima();
        DashboardOperacionalResumo.IntegracoesResumo integracoes = integracoes();
        var tendencias = tendenciasService.montar();
        NivelAtencao nivel = nivelAtencao(tarefas, alertas, estoque, integracoes);

        DashboardOperacionalResumo resumo = new DashboardOperacionalResumo(
                nivel, mensagemAtencao(nivel), tarefas, alertas, estoque, compras, clima, integracoes,
                tendencias,
                LocalDateTime.now(clock));
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "dashboard.loaded",
                "module", "dashboard",
                "event.duration", System.nanoTime() - inicio,
                "dashboard.alertas.ativos", alertas.ativos(),
                "dashboard.tarefas.vencidas", tarefas.vencidas(),
                "dashboard.estoque.itens_criticos", estoque.itensAbaixoMinimo(),
                "dashboard.clima.estado", clima.estado().name()))) {
            log.info("Dashboard operacional carregado.");
        }
        return resumo;
    }

    private DashboardOperacionalResumo.TarefasResumo tarefas(TarefasAlertasPainelResumo.Tarefas origem) {
        List<DashboardOperacionalResumo.TarefaItem> itens = origem.destaques().stream()
                .map(item -> new DashboardOperacionalResumo.TarefaItem(
                        item.id(), item.titulo(), item.status(), item.prioridade(), item.dataVencimento(),
                        item.responsavelNome(), item.vencida()))
                .toList();
        return new DashboardOperacionalResumo.TarefasResumo(
                origem.abertas(), origem.pendentesHoje(), origem.vencidas(), origem.criticas(),
                origem.emAndamento(), itens);
    }

    private DashboardOperacionalResumo.AlertasResumo alertas(TarefasAlertasPainelResumo.Alertas origem) {
        List<DashboardOperacionalResumo.AlertaItem> itens = origem.destaques().stream()
                .map(item -> new DashboardOperacionalResumo.AlertaItem(
                        item.id(), item.titulo(), item.severidade(), item.status(),
                        item.moduloOrigem(), item.detectadoEm()))
                .toList();
        return new DashboardOperacionalResumo.AlertasResumo(
                origem.ativos(), origem.criticos(), origem.altaSeveridade(), itens);
    }

    private DashboardOperacionalResumo.EstoqueResumo estoque() {
        List<ItemEstoqueResumo> itensCriticos = estoqueService.listarItensComSaldo().stream()
                .filter(ItemEstoqueResumo::ativo)
                .filter(ItemEstoqueResumo::estoqueBaixo)
                .toList();
        List<LoteEstoqueResumo> vencidos = estoqueService.listarLotesVencidos();
        List<LoteEstoqueResumo> proximos = estoqueService.listarLotesProximosVencimento(DIAS_LOTE_PROXIMO);
        List<DashboardOperacionalResumo.EstoqueItem> destaques = itensCriticos.stream()
                .limit(LIMITE_DESTAQUES)
                .map(item -> new DashboardOperacionalResumo.EstoqueItem(
                        item.id(), item.nome(), item.unidade(), item.saldo(), item.estoqueMinimo()))
                .toList();
        return new DashboardOperacionalResumo.EstoqueResumo(
                itensCriticos.size(), vencidos.size(), proximos.size(), destaques);
    }

    private DashboardOperacionalResumo.ComprasResumo compras(ComprasDashboardResumo origem) {
        List<DashboardOperacionalResumo.CompraItem> recentes = origem.comprasRecentes().stream()
                .limit(LIMITE_DESTAQUES)
                .map(this::compra)
                .toList();
        return new DashboardOperacionalResumo.ComprasResumo(
                origem.rascunhos(), origem.comprasConfirmadasNoMes(), origem.valorConfirmadoNoMes(),
                origem.ultimaCompraConfirmada() == null ? null : compra(origem.ultimaCompraConfirmada()), recentes);
    }

    private DashboardOperacionalResumo.CompraItem compra(CompraResumo compra) {
        return new DashboardOperacionalResumo.CompraItem(
                compra.id(), compra.fornecedorNome(), compra.dataCompra(), compra.status(), compra.total());
    }

    private DashboardOperacionalResumo.ClimaResumo clima() {
        ClimaResumo origem = climaService.resumo();
        if (origem == null || !origem.disponivel()) {
            return new DashboardOperacionalResumo.ClimaResumo(
                    EstadoClimaDashboard.SEM_DADOS, null, "Previsão ainda não sincronizada",
                    null, null, null, null);
        }
        Integer probabilidade = climaService.previsao(24).stream()
                .map(PrevisaoClimaticaResponse::probabilidadePrecipitacao)
                .filter(valor -> valor != null)
                .max(Integer::compareTo)
                .orElse(null);
        EstadoClimaDashboard estado = origem.desatualizado()
                ? EstadoClimaDashboard.DESATUALIZADO
                : EstadoClimaDashboard.NORMAL;
        return new DashboardOperacionalResumo.ClimaResumo(
                estado, origem.temperatura(), condicaoClima(origem.codigoTempo()),
                origem.chuvaProximas24h(), probabilidade, origem.velocidadeVento(), origem.ultimaAtualizacao());
    }

    private DashboardOperacionalResumo.IntegracoesResumo integracoes() {
        List<IntegracaoFonteResumo> implementadas = integracaoService.resumo().fontesPorGrupo().values().stream()
                .flatMap(List::stream)
                .filter(IntegracaoFonteResumo::implementada)
                .toList();
        long operacionais = contar(implementadas, StatusOperacionalIntegracao.OPERACIONAL);
        long desatualizadas = contar(implementadas, StatusOperacionalIntegracao.DESATUALIZADO);
        long falhas = contar(implementadas, StatusOperacionalIntegracao.FALHA);
        long naoConfiguradas = contar(implementadas, StatusOperacionalIntegracao.NAO_CONFIGURADO);
        List<DashboardOperacionalResumo.IntegracaoItem> problemas = implementadas.stream()
                .filter(item -> item.status() == StatusOperacionalIntegracao.FALHA
                        || item.status() == StatusOperacionalIntegracao.DESATUALIZADO)
                .sorted(Comparator.comparingInt(item -> prioridade(item.status())))
                .limit(LIMITE_DESTAQUES)
                .map(item -> new DashboardOperacionalResumo.IntegracaoItem(
                        item.slug(), item.nome(), item.status(), item.ultimaTentativa()))
                .toList();
        return new DashboardOperacionalResumo.IntegracoesResumo(
                implementadas.size(), operacionais, desatualizadas, falhas, naoConfiguradas, problemas);
    }

    private long contar(List<IntegracaoFonteResumo> fontes, StatusOperacionalIntegracao status) {
        return fontes.stream().filter(item -> item.status() == status).count();
    }

    private int prioridade(StatusOperacionalIntegracao status) {
        return status == StatusOperacionalIntegracao.FALHA ? 0 : 1;
    }

    private NivelAtencao nivelAtencao(DashboardOperacionalResumo.TarefasResumo tarefas,
            DashboardOperacionalResumo.AlertasResumo alertas,
            DashboardOperacionalResumo.EstoqueResumo estoque,
            DashboardOperacionalResumo.IntegracoesResumo integracoes) {
        if (alertas.criticos() > 0 || tarefas.criticas() > 0) {
            return NivelAtencao.CRITICA;
        }
        if (tarefas.vencidas() > 0 || alertas.altaSeveridade() > 0
                || estoque.lotesVencidos() > 0 || integracoes.comFalha() > 0) {
            return NivelAtencao.ALTA;
        }
        if (tarefas.pendentesHoje() > 0 || alertas.ativos() > 0 || estoque.itensAbaixoMinimo() > 0
                || estoque.lotesProximosVencimento() > 0 || integracoes.desatualizadas() > 0) {
            return NivelAtencao.ATENCAO;
        }
        return NivelAtencao.NORMAL;
    }

    private String mensagemAtencao(NivelAtencao nivel) {
        return switch (nivel) {
            case NORMAL -> "Operação sem pendências prioritárias neste momento.";
            case ATENCAO -> "Há itens para acompanhar na rotina de hoje.";
            case ALTA -> "Existem pendências importantes que precisam de ação.";
            case CRITICA -> "Existem ocorrências críticas que precisam de atenção imediata.";
        };
    }

    private String condicaoClima(Integer codigo) {
        if (codigo == null) {
            return "Condição não informada";
        }
        return switch (codigo) {
            case 0 -> "Céu limpo";
            case 1, 2 -> "Parcialmente nublado";
            case 3 -> "Nublado";
            case 45, 48 -> "Neblina";
            case 51, 53, 55, 56, 57 -> "Garoa";
            case 61, 63, 65, 66, 67 -> "Chuva";
            case 71, 73, 75, 77 -> "Precipitação congelada";
            case 80, 81, 82 -> "Pancadas de chuva";
            case 85, 86 -> "Pancadas de neve";
            case 95, 96, 99 -> "Trovoadas";
            default -> "Condição variável";
        };
    }
}
