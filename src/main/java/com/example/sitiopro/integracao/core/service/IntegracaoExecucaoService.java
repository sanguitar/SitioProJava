package com.example.sitiopro.integracao.core.service;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.IntegracaoOperacaoException;
import com.example.sitiopro.integracao.core.ResultadoSincronizacao;
import com.example.sitiopro.integracao.core.StatusExecucaoIntegracao;
import com.example.sitiopro.integracao.core.config.IntegracaoCoreProperties;
import com.example.sitiopro.integracao.core.dto.IntegracaoExecucaoResumo;
import com.example.sitiopro.integracao.core.entity.IntegracaoEstado;
import com.example.sitiopro.integracao.core.entity.IntegracaoExecucao;
import com.example.sitiopro.integracao.core.repository.IntegracaoEstadoRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoExecucaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class IntegracaoExecucaoService {

    private final IntegracaoEstadoRepository estadoRepository;
    private final IntegracaoExecucaoRepository execucaoRepository;
    private final IntegracaoCoreProperties properties;
    private final Clock clock;

    public IntegracaoExecucaoService(IntegracaoEstadoRepository estadoRepository,
            IntegracaoExecucaoRepository execucaoRepository, IntegracaoCoreProperties properties, Clock clock) {
        this.estadoRepository = estadoRepository;
        this.execucaoRepository = execucaoRepository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public IntegracaoExecucao iniciar(FonteIntegracao fonte, String traceId) {
        LocalDateTime agora = LocalDateTime.now(clock);
        IntegracaoEstado estado = estadoRepository.buscarParaAtualizacao(fonte)
                .orElseGet(() -> estadoRepository.saveAndFlush(new IntegracaoEstado(fonte)));
        if (estado.isEmExecucao() && !lockExpirado(estado, agora)) {
            throw new IntegracaoOperacaoException(
                    "INTEGRACAO_EM_EXECUCAO",
                    "Já existe uma sincronização em andamento para " + fonte.getNome() + ".",
                    HttpStatus.CONFLICT);
        }

        estado.setUltimaTentativaEm(agora);
        estado.setEmExecucao(true);
        estado.setExecucaoIniciadaEm(agora);
        IntegracaoExecucao execucao = new IntegracaoExecucao(fonte, agora, traceId);
        return execucaoRepository.save(execucao);
    }

    @Transactional
    public IntegracaoExecucaoResumo concluir(Long execucaoId, ResultadoSincronizacao resultado) {
        LocalDateTime agora = LocalDateTime.now(clock);
        IntegracaoExecucao execucao = buscarExecucao(execucaoId);
        IntegracaoEstado estado = buscarEstadoParaAtualizacao(execucao.getFonte());

        execucao.setFinalizadoEm(agora);
        execucao.setStatus(StatusExecucaoIntegracao.SUCCESS);
        execucao.setRegistrosLidos(resultado.lidos());
        execucao.setRegistrosInseridos(resultado.inseridos());
        execucao.setRegistrosAtualizados(resultado.atualizados());
        execucao.setRegistrosIgnorados(resultado.ignorados());
        estado.setUltimoSucessoEm(agora);
        liberar(estado);
        return IntegracaoExecucaoResumo.de(execucao);
    }

    @Transactional
    public IntegracaoExecucaoResumo falhar(Long execucaoId, String codigo, String resumoSeguro) {
        IntegracaoExecucao execucao = buscarExecucao(execucaoId);
        IntegracaoEstado estado = buscarEstadoParaAtualizacao(execucao.getFonte());
        execucao.setFinalizadoEm(LocalDateTime.now(clock));
        execucao.setStatus(StatusExecucaoIntegracao.FAILURE);
        execucao.setErroCodigo(limitar(codigo, 80));
        execucao.setErroResumo(limitar(resumoSeguro, 500));
        liberar(estado);
        return IntegracaoExecucaoResumo.de(execucao);
    }

    @Transactional(readOnly = true)
    public Optional<IntegracaoEstado> buscarEstado(FonteIntegracao fonte) {
        return estadoRepository.findById(fonte);
    }

    @Transactional(readOnly = true)
    public Optional<IntegracaoExecucaoResumo> buscarUltimaExecucao(FonteIntegracao fonte) {
        return execucaoRepository.findFirstByFonteOrderByIniciadoEmDescIdDesc(fonte)
                .map(IntegracaoExecucaoResumo::de);
    }

    @Transactional(readOnly = true)
    public List<IntegracaoExecucaoResumo> listarHistorico(FonteIntegracao fonte) {
        int limite = Math.max(1, Math.min(properties.getHistoryLimit(), 100));
        return execucaoRepository.findByFonteOrderByIniciadoEmDescIdDesc(fonte, PageRequest.of(0, limite))
                .stream()
                .map(IntegracaoExecucaoResumo::de)
                .toList();
    }

    private boolean lockExpirado(IntegracaoEstado estado, LocalDateTime agora) {
        return estado.getExecucaoIniciadaEm() == null
                || estado.getExecucaoIniciadaEm().isBefore(agora.minus(properties.getRunningTimeout()));
    }

    private IntegracaoExecucao buscarExecucao(Long id) {
        return execucaoRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Execução de integração não encontrada."));
    }

    private IntegracaoEstado buscarEstadoParaAtualizacao(FonteIntegracao fonte) {
        return estadoRepository.buscarParaAtualizacao(fonte)
                .orElseThrow(() -> new IllegalStateException("Estado de integração não encontrado."));
    }

    private void liberar(IntegracaoEstado estado) {
        estado.setEmExecucao(false);
        estado.setExecucaoIniciadaEm(null);
    }

    private String limitar(String valor, int tamanho) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }
}
