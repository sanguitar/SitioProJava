package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.criacao.aves.service.AvesAlertasService;
import com.example.sitiopro.criacao.suinos.service.SuinosAlertasService;
import com.example.sitiopro.criacao.suinos.service.SuinosSanidadeAlertasService;
import com.example.sitiopro.estoque.dto.LoteEstoqueResumo;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;
import com.example.sitiopro.integracao.core.dto.IntegracaoFonteResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegrasAlertasService {

    private static final Logger log = LoggerFactory.getLogger(RegrasAlertasService.class);

    private final EstoqueMovimentoService estoqueService;
    private final IntegracaoPainelService integracaoService;
    private final ClimaConsultaService climaService;
    private final AlertaService alertaService;
    private final TarefasAlertasProperties properties;
    private final AvesAlertasService avesAlertasService;
    private final SuinosAlertasService suinosAlertasService;
    private final SuinosSanidadeAlertasService suinosSanidadeAlertasService;

    public RegrasAlertasService(EstoqueMovimentoService estoqueService,
            IntegracaoPainelService integracaoService,
            ClimaConsultaService climaService,
            AlertaService alertaService,
            TarefasAlertasProperties properties,
            AvesAlertasService avesAlertasService,
            SuinosAlertasService suinosAlertasService,
            SuinosSanidadeAlertasService suinosSanidadeAlertasService) {
        this.estoqueService = estoqueService;
        this.integracaoService = integracaoService;
        this.climaService = climaService;
        this.alertaService = alertaService;
        this.properties = properties;
        this.avesAlertasService = avesAlertasService;
        this.suinosAlertasService = suinosAlertasService;
        this.suinosSanidadeAlertasService = suinosSanidadeAlertasService;
    }

    public void avaliar() {
        avaliarRegra("estoque-minimo", this::avaliarEstoqueMinimo);
        avaliarRegra("lotes-proximos", this::avaliarLotesProximos);
        avaliarRegra("lotes-vencidos", this::avaliarLotesVencidos);
        avaliarRegra("integracoes", this::avaliarIntegracoes);
        avaliarRegra("clima", this::avaliarChuva);
        avaliarRegra("criacoes-aves", avesAlertasService::avaliar);
        avaliarRegra("criacoes-suinos", suinosAlertasService::avaliar);
        avaliarRegra("criacoes-suinos-sanidade", suinosSanidadeAlertasService::avaliar);
    }

    private void avaliarEstoqueMinimo() {
        List<CondicaoAlerta> condicoes = estoqueService.listarItensComSaldo().stream()
                .filter(ItemEstoqueResumo::ativo)
                .filter(ItemEstoqueResumo::estoqueBaixo)
                .map(item -> new CondicaoAlerta(
                        "ESTOQUE:ITEM:" + item.id() + ":ABAIXO_MINIMO",
                        item.nome() + " abaixo do estoque mínimo",
                        "Saldo atual de " + item.saldo() + " " + item.unidade()
                                + ", abaixo do mínimo de " + item.estoqueMinimo() + " " + item.unidade() + ".",
                        SeveridadeAlerta.ALTA,
                        "ITEM:" + item.id(),
                        contextoItem(item)))
                .toList();
        alertaService.sincronizar(ModuloOrigem.ESTOQUE, TipoAlerta.ESTOQUE_ABAIXO_MINIMO, condicoes);
    }

    private void avaliarLotesProximos() {
        List<CondicaoAlerta> condicoes = estoqueService
                .listarLotesProximosVencimento(properties.getLoteProximoVencimentoDias()).stream()
                .map(lote -> new CondicaoAlerta(
                        "ESTOQUE:LOTE:" + lote.id() + ":PROXIMO_VENCIMENTO",
                        "Lote " + lote.codigo() + " próximo do vencimento",
                        lote.itemNome() + " vence em " + lote.validade() + ".",
                        SeveridadeAlerta.ATENCAO,
                        "LOTE:" + lote.id(),
                        contextoLote(lote)))
                .toList();
        alertaService.sincronizar(ModuloOrigem.ESTOQUE, TipoAlerta.LOTE_PROXIMO_VENCIMENTO, condicoes);
    }

    private void avaliarLotesVencidos() {
        List<CondicaoAlerta> condicoes = estoqueService.listarLotesVencidos().stream()
                .map(lote -> new CondicaoAlerta(
                        "ESTOQUE:LOTE:" + lote.id() + ":VENCIDO",
                        "Lote " + lote.codigo() + " vencido",
                        lote.itemNome() + " venceu em " + lote.validade() + " e ainda possui saldo.",
                        SeveridadeAlerta.CRITICA,
                        "LOTE:" + lote.id(),
                        contextoLote(lote)))
                .toList();
        alertaService.sincronizar(ModuloOrigem.ESTOQUE, TipoAlerta.LOTE_VENCIDO, condicoes);
    }

    private void avaliarIntegracoes() {
        List<IntegracaoFonteResumo> fontes = integracaoService.resumo().fontesPorGrupo().values().stream()
                .flatMap(List::stream)
                .filter(IntegracaoFonteResumo::implementada)
                .filter(IntegracaoFonteResumo::habilitada)
                .filter(IntegracaoFonteResumo::configurada)
                .toList();
        List<CondicaoAlerta> desatualizadas = fontes.stream()
                .filter(fonte -> fonte.status() == StatusOperacionalIntegracao.DESATUALIZADO)
                .map(fonte -> condicaoIntegracao(fonte, "DESATUALIZADA", SeveridadeAlerta.ALTA))
                .toList();
        List<CondicaoAlerta> falhas = fontes.stream()
                .filter(fonte -> fonte.status() == StatusOperacionalIntegracao.FALHA)
                .map(fonte -> condicaoIntegracao(fonte, "FALHA", SeveridadeAlerta.ALTA))
                .toList();
        alertaService.sincronizar(ModuloOrigem.INTEGRACOES, TipoAlerta.INTEGRACAO_DESATUALIZADA, desatualizadas);
        alertaService.sincronizar(ModuloOrigem.INTEGRACOES, TipoAlerta.INTEGRACAO_COM_FALHA, falhas);
    }

    private void avaliarChuva() {
        ClimaResumo clima = climaService.resumo();
        if (!clima.disponivel() || clima.chuvaProximas24h() == null) {
            return;
        }
        List<CondicaoAlerta> condicoes = new ArrayList<>();
        if (clima.chuvaProximas24h().compareTo(properties.getChuva24hLimiteMm()) >= 0) {
            Map<String, Object> contexto = new LinkedHashMap<>();
            contexto.put("chuva24hMm", clima.chuvaProximas24h());
            contexto.put("limiteMm", properties.getChuva24hLimiteMm());
            contexto.put("fonte", clima.fonte());
            condicoes.add(new CondicaoAlerta(
                    "CLIMA:CONTEXTO:PRINCIPAL:CHUVA_24H",
                    "Previsão de chuva intensa nas próximas 24 horas",
                    "Acumulado previsto de " + clima.chuvaProximas24h()
                            + " mm, acima do limite de " + properties.getChuva24hLimiteMm() + " mm.",
                    SeveridadeAlerta.ALTA,
                    "CONTEXTO:PRINCIPAL",
                    contexto));
        }
        alertaService.sincronizar(ModuloOrigem.CLIMA, TipoAlerta.CHUVA_INTENSA_24H, condicoes);
    }

    private CondicaoAlerta condicaoIntegracao(IntegracaoFonteResumo fonte, String sufixo,
            SeveridadeAlerta severidade) {
        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("fonte", fonte.slug());
        if (fonte.ultimoSucesso() != null) {
            contexto.put("ultimoSucesso", fonte.ultimoSucesso());
        }
        if (fonte.ultimaTentativa() != null) {
            contexto.put("ultimaTentativa", fonte.ultimaTentativa());
        }
        contexto.put("status", fonte.status().name());
        return new CondicaoAlerta(
                "INTEGRACAO:" + fonte.slug().toUpperCase().replace('-', '_') + ":" + sufixo,
                fonte.nome() + ("FALHA".equals(sufixo) ? " com falha" : " desatualizada"),
                "O estado operacional persistido da integração requer atenção.",
                severidade,
                "INTEGRACAO:" + fonte.slug(),
                contexto);
    }

    private Map<String, Object> contextoItem(ItemEstoqueResumo item) {
        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("itemId", item.id());
        contexto.put("saldo", item.saldo());
        contexto.put("estoqueMinimo", item.estoqueMinimo());
        contexto.put("unidade", item.unidade());
        return contexto;
    }

    private Map<String, Object> contextoLote(LoteEstoqueResumo lote) {
        Map<String, Object> contexto = new LinkedHashMap<>();
        contexto.put("loteId", lote.id());
        contexto.put("codigo", lote.codigo());
        contexto.put("validade", lote.validade());
        contexto.put("saldo", lote.saldo());
        contexto.put("unidade", lote.unidade());
        return contexto;
    }

    private void avaliarRegra(String regra, Runnable avaliacao) {
        try {
            avaliacao.run();
        } catch (RuntimeException ex) {
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", "alerta.rule.failed",
                    "module", "tarefas",
                    "alerta.regra", regra,
                    "error.type", ex.getClass().getName()))) {
                log.error("Falha ao avaliar regra automática de alerta: {}.", regra);
            }
        }
    }
}
