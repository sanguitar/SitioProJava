package com.example.sitiopro.integracao.core.service;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.IntegracaoSincronizador;
import com.example.sitiopro.integracao.core.StatusExecucaoIntegracao;
import com.example.sitiopro.integracao.core.StatusOperacionalIntegracao;
import com.example.sitiopro.integracao.core.dto.IntegracaoExecucaoResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoFonteResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.entity.IntegracaoEstado;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IntegracaoPainelService {

    private static final List<FonteFutura> FONTES_FUTURAS = List.of(
            new FonteFutura("mapa-zarc", "MAPA ZARC", "Agronomia", "Zoneamento agrícola de risco climático."),
            new FonteFutura("embrapa-bioinsumos", "Embrapa Bioinsumos", "Agronomia", "Catálogo oficial de bioinsumos."),
            new FonteFutura("embrapa-respondeagro", "Embrapa RespondeAgro", "Agronomia", "Conteúdo técnico sob demanda."),
            new FonteFutura("embrapa-smartsolos", "Embrapa SmartSolos", "Agronomia", "Interpretação futura de dados de solo."),
            new FonteFutura("nasa-power", "NASA POWER", "Estatísticas / Território", "Séries meteorológicas e agroclimáticas."),
            new FonteFutura("ibge-sidra", "IBGE SIDRA", "Estatísticas / Território", "Indicadores agropecuários e territoriais."),
            new FonteFutura("ibge-localidades", "IBGE Localidades", "Estatísticas / Território", "Referências territoriais oficiais."),
            new FonteFutura("inpe-terrabrasilis", "INPE TerraBrasilis", "Estatísticas / Território", "Dados territoriais e ambientais."),
            new FonteFutura("embrapa-agrotermos", "Embrapa AgroTermos", "Estatísticas / Território", "Vocabulário agropecuário controlado."),
            new FonteFutura("embrapa-bovtrace", "Embrapa BovTrace", "Pesquisa futura", "Rastreabilidade bovina futura."),
            new FonteFutura("embrapa-plantannot", "Embrapa PlantAnnot", "Pesquisa futura", "Anotações e referências vegetais futuras."),
            new FonteFutura("embrapa-sting", "Embrapa Sting", "Pesquisa futura", "Fonte de pesquisa para avaliação posterior."));

    private final Map<FonteIntegracao, IntegracaoSincronizador> sincronizadores;
    private final IntegracaoExecucaoService execucaoService;
    private final Clock clock;

    public IntegracaoPainelService(List<IntegracaoSincronizador> sincronizadores,
            IntegracaoExecucaoService execucaoService, Clock clock) {
        this.sincronizadores = sincronizadores.stream()
                .collect(Collectors.toMap(IntegracaoSincronizador::fonte, item -> item));
        this.execucaoService = execucaoService;
        this.clock = clock;
    }

    public IntegracaoPainelResumo resumo() {
        List<IntegracaoFonteResumo> fontes = new ArrayList<>();
        sincronizadores.values().stream()
                .sorted((a, b) -> a.fonte().getNome().compareToIgnoreCase(b.fonte().getNome()))
                .map(this::montarImplementada)
                .forEach(fontes::add);
        FONTES_FUTURAS.stream().map(this::montarFutura).forEach(fontes::add);

        Map<String, List<IntegracaoFonteResumo>> porGrupo = fontes.stream()
                .collect(Collectors.groupingBy(
                        IntegracaoFonteResumo::grupo,
                        LinkedHashMap::new,
                        Collectors.toList()));
        long operacionais = contar(fontes, StatusOperacionalIntegracao.OPERACIONAL);
        long naoConfiguradas = contar(fontes, StatusOperacionalIntegracao.NAO_CONFIGURADO);
        long falhas = contar(fontes, StatusOperacionalIntegracao.FALHA);
        long comAtencao = fontes.stream()
                .filter(item -> item.status() == StatusOperacionalIntegracao.DESATUALIZADO
                        || item.status() == StatusOperacionalIntegracao.FALHA)
                .count();
        return new IntegracaoPainelResumo(operacionais, comAtencao, naoConfiguradas, falhas, porGrupo);
    }

    public IntegracaoFonteResumo detalhar(FonteIntegracao fonte) {
        IntegracaoSincronizador sincronizador = sincronizadores.get(fonte);
        if (sincronizador == null) {
            throw new IntegracaoOperacaoException(
                    "INTEGRACAO_NAO_IMPLEMENTADA", "Esta integração ainda não possui detalhe operacional.",
                    HttpStatus.NOT_FOUND);
        }
        return montarImplementada(sincronizador);
    }

    public List<IntegracaoExecucaoResumo> historico(FonteIntegracao fonte) {
        detalhar(fonte);
        return execucaoService.listarHistorico(fonte);
    }

    private IntegracaoFonteResumo montarImplementada(IntegracaoSincronizador sincronizador) {
        FonteIntegracao fonte = sincronizador.fonte();
        Optional<IntegracaoEstado> estado = execucaoService.buscarEstado(fonte);
        Optional<IntegracaoExecucaoResumo> ultimaExecucao = execucaoService.buscarUltimaExecucao(fonte);
        StatusOperacionalIntegracao status = calcularStatus(sincronizador, estado, ultimaExecucao);
        return new IntegracaoFonteResumo(
                fonte.getSlug(),
                fonte.getNome(),
                fonte.getGrupo(),
                descricao(fonte),
                true,
                sincronizador.habilitada(),
                sincronizador.configurada(),
                estado.map(IntegracaoEstado::isEmExecucao).orElse(false),
                sincronizador.habilitada() && sincronizador.configurada(),
                sincronizador.usaCredencial()
                        ? (sincronizador.configurada() ? "CONFIGURADA" : "NÃO CONFIGURADA")
                        : "NÃO EXIGIDA",
                status,
                estado.map(IntegracaoEstado::getUltimoSucessoEm).orElse(null),
                estado.map(IntegracaoEstado::getUltimaTentativaEm).orElse(null),
                proximaExecucao(sincronizador),
                ultimaExecucao.orElse(null));
    }

    private IntegracaoFonteResumo montarFutura(FonteFutura fonte) {
        return new IntegracaoFonteResumo(
                fonte.slug(), fonte.nome(), fonte.grupo(), fonte.descricao(),
                false, false, false, false, false, "NÃO APLICÁVEL",
                StatusOperacionalIntegracao.NAO_INICIALIZADO,
                null, null, null, null);
    }

    private StatusOperacionalIntegracao calcularStatus(IntegracaoSincronizador sincronizador,
            Optional<IntegracaoEstado> estado, Optional<IntegracaoExecucaoResumo> ultimaExecucao) {
        if (!sincronizador.habilitada()) {
            return StatusOperacionalIntegracao.DESABILITADO;
        }
        if (!sincronizador.configurada()) {
            return StatusOperacionalIntegracao.NAO_CONFIGURADO;
        }
        if (ultimaExecucao.isPresent()
                && ultimaExecucao.get().status() == StatusExecucaoIntegracao.FAILURE) {
            return StatusOperacionalIntegracao.FALHA;
        }
        LocalDateTime ultimoSucesso = estado.map(IntegracaoEstado::getUltimoSucessoEm).orElse(null);
        if (ultimoSucesso == null) {
            return StatusOperacionalIntegracao.NAO_INICIALIZADO;
        }
        if (ultimoSucesso.isBefore(LocalDateTime.now(clock).minus(sincronizador.limiteDesatualizacao()))) {
            return StatusOperacionalIntegracao.DESATUALIZADO;
        }
        return StatusOperacionalIntegracao.OPERACIONAL;
    }

    private LocalDateTime proximaExecucao(IntegracaoSincronizador sincronizador) {
        if (!sincronizador.habilitada()) {
            return null;
        }
        try {
            ZonedDateTime agora = ZonedDateTime.now(clock).withZoneSameInstant(sincronizador.zonaAgendamento());
            ZonedDateTime proxima = CronExpression.parse(sincronizador.cron()).next(agora);
            return proxima == null ? null : proxima.toLocalDateTime();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private long contar(List<IntegracaoFonteResumo> fontes, StatusOperacionalIntegracao status) {
        return fontes.stream().filter(item -> item.status() == status).count();
    }

    private String descricao(FonteIntegracao fonte) {
        return switch (fonte) {
            case OPEN_METEO -> "Previsão horária persistida para o dashboard e decisões agrícolas.";
            case EMBRAPA_AGROFIT -> "Piloto controlado do catálogo oficial de culturas do Agrofit.";
        };
    }

    private record FonteFutura(String slug, String nome, String grupo, String descricao) {
    }
}
