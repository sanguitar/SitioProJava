package com.example.sitiopro.integracao.core.service;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoHttpException;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.IntegracaoSincronizador;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.integracao.core.dto.IntegracaoExecucaoResumo;
import com.example.sitiopro.integracao.core.entity.IntegracaoExecucao;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.shared.observability.RequestCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class IntegracaoOrquestrador {

    private static final Logger log = LoggerFactory.getLogger(IntegracaoOrquestrador.class);
    private static final Pattern SENSITIVE_VALUE_PATTERN = Pattern.compile(
            "(?i)(senha|password|passwd|pwd|secret|token|authorization|cookie|api[-_ ]?key)\\s*[:=]\\s*\\S+");

    private final Map<FonteIntegracao, IntegracaoSincronizador> sincronizadores;
    private final IntegracaoExecucaoService execucaoService;

    public IntegracaoOrquestrador(List<IntegracaoSincronizador> sincronizadores,
            IntegracaoExecucaoService execucaoService) {
        this.sincronizadores = new EnumMap<>(FonteIntegracao.class);
        sincronizadores.forEach(sincronizador -> this.sincronizadores.put(sincronizador.fonte(), sincronizador));
        this.execucaoService = execucaoService;
    }

    public IntegracaoExecucaoResumo sincronizar(FonteIntegracao fonte) {
        IntegracaoSincronizador sincronizador = buscarSincronizador(fonte);
        validarDisponibilidade(sincronizador);

        String requestId = RequestCorrelation.currentRequestId();
        String traceId = valorOuPadrao(MDC.get("trace.id"), requestId);
        try (MdcScope ignored = MdcScope.with(Map.of(
                RequestCorrelation.MDC_REQUEST_ID, requestId,
                "module", "integracao",
                "integration.source", fonte.getSlug()))) {
            IntegracaoExecucao execucao = execucaoService.iniciar(fonte, traceId);
            logInicio(fonte, execucao.getId());
            long inicio = System.nanoTime();
            try {
                ResultadoSincronizacao resultado = sincronizador.sincronizar();
                IntegracaoExecucaoResumo concluida = execucaoService.concluir(execucao.getId(), resultado);
                logConclusao(fonte, concluida, System.nanoTime() - inicio);
                return concluida;
            } catch (RuntimeException ex) {
                String codigo = codigoErro(ex);
                IntegracaoExecucaoResumo falha = execucaoService.falhar(
                        execucao.getId(), codigo, resumoSeguro(ex));
                logFalha(fonte, falha, System.nanoTime() - inicio, ex);
                throw new IntegracaoOperacaoException(
                        codigo,
                        "A sincronização de " + fonte.getNome()
                                + " falhou. Os dados locais anteriores foram preservados.",
                        HttpStatus.SERVICE_UNAVAILABLE);
            }
        }
    }

    public IntegracaoSincronizador buscarSincronizador(FonteIntegracao fonte) {
        IntegracaoSincronizador sincronizador = sincronizadores.get(fonte);
        if (sincronizador == null) {
            throw new IntegracaoOperacaoException(
                    "INTEGRACAO_NAO_IMPLEMENTADA", "Esta integração ainda não foi implementada.",
                    HttpStatus.NOT_FOUND);
        }
        return sincronizador;
    }

    private void validarDisponibilidade(IntegracaoSincronizador sincronizador) {
        if (!sincronizador.habilitada()) {
            throw new IntegracaoOperacaoException(
                    "INTEGRACAO_DESABILITADA", "A integração está desabilitada.", HttpStatus.CONFLICT);
        }
        if (!sincronizador.configurada()) {
            throw new IntegracaoOperacaoException(
                    "INTEGRACAO_NAO_CONFIGURADA", "A integração ainda não possui configuração válida.",
                    HttpStatus.CONFLICT);
        }
    }

    private void logInicio(FonteIntegracao fonte, Long execucaoId) {
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "integration.sync.started",
                "integration.source", fonte.getSlug(),
                "integration.execution.id", execucaoId))) {
            log.info("Sincronização iniciada para {}", fonte.getSlug());
        }
    }

    private void logConclusao(FonteIntegracao fonte, IntegracaoExecucaoResumo execucao, long duracao) {
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "integration.sync.completed",
                "integration.source", fonte.getSlug(),
                "integration.execution.id", execucao.id(),
                "records.read", execucao.registrosLidos(),
                "records.inserted", execucao.registrosInseridos(),
                "records.updated", execucao.registrosAtualizados(),
                "records.ignored", execucao.registrosIgnorados(),
                "event.duration", duracao))) {
            log.info("Sincronização concluída para {}", fonte.getSlug());
        }
    }

    private void logFalha(FonteIntegracao fonte, IntegracaoExecucaoResumo execucao, long duracao,
            RuntimeException ex) {
        Map<String, Object> campos = new java.util.LinkedHashMap<>();
        campos.put("event.action", "integration.sync.failed");
        campos.put("integration.source", fonte.getSlug());
        campos.put("integration.execution.id", execucao.id());
        campos.put("error.code", execucao.erroCodigo());
        campos.put("error.type", ex.getClass().getName());
        campos.put("event.duration", duracao);
        try (MdcScope ignored = MdcScope.with(campos)) {
            log.error("Sincronização falhou para {}: {}", fonte.getSlug(), execucao.erroCodigo());
        }
    }

    private String codigoErro(RuntimeException ex) {
        if (ex instanceof IntegracaoHttpException httpException) {
            return httpException.getCode();
        }
        if (ex instanceof IntegracaoOperacaoException operacaoException) {
            return operacaoException.getCode();
        }
        return "INTEGRACAO_FALHA_INESPERADA";
    }

    private String resumoSeguro(RuntimeException ex) {
        String mensagem = ex.getMessage();
        if (mensagem == null || mensagem.isBlank()) {
            return "Falha sem detalhes seguros disponíveis.";
        }
        return SENSITIVE_VALUE_PATTERN.matcher(mensagem).replaceAll("$1=<redacted>");
    }

    private String valorOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }
}
