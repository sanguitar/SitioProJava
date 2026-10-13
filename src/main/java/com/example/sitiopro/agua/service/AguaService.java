package com.example.sitiopro.agua.service;

import com.example.sitiopro.agua.dto.*;
import com.example.sitiopro.agua.entity.*;
import com.example.sitiopro.agua.repository.OperacaoAguaRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.manutencao.service.ManutencaoService;
import com.example.sitiopro.propriedade.entity.RecursoHidrico;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.tarefas.dto.*;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AguaService {
    private final RecursoHidricoRepository recursos;
    private final PropriedadeRepository propriedades;
    private final OperacaoAguaRepository operacoes;
    private final CodigoCriacaoService locks;
    private final TarefaService tarefas;
    private final AguaAlertasService alertas;
    private final ManutencaoService manutencao;
    private final Clock clock;

    public AguaService(RecursoHidricoRepository recursos, PropriedadeRepository propriedades,
            OperacaoAguaRepository operacoes, CodigoCriacaoService locks, TarefaService tarefas,
            AguaAlertasService alertas, ManutencaoService manutencao, Clock clock) {
        this.recursos = recursos;
        this.propriedades = propriedades;
        this.operacoes = operacoes;
        this.locks = locks;
        this.tarefas = tarefas;
        this.alertas = alertas;
        this.manutencao = manutencao;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<RecursoHidricoOperacionalResumo> listarRecursos() {
        return recursos.findByPropriedadeIdOrderByNomeAsc(propriedadeId()).stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public RecursoHidricoOperacionalResumo detalharRecurso(Long id) {
        return resumo(buscar(id));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<OperacaoAguaResumo> listarOperacoes(int pagina, int tamanho) {
        return PaginaResponse.de(operacoes.findByRecursoOrigemPropriedadeIdOrderByDataOperacaoDescIdDesc(
                propriedadeId(), PageRequest.of(limitarPagina(pagina), limitarTamanho(tamanho))).map(this::resumo));
    }

    @Transactional(readOnly = true)
    public List<OperacaoAguaResumo> listarOperacoesDoRecurso(Long recursoId) {
        buscar(recursoId);
        return operacoes.listarDoRecurso(recursoId, PageRequest.of(0, 50)).stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public AguaDashboardResumo dashboard() {
        List<RecursoHidricoOperacionalResumo> lista = listarRecursos();
        var ativos = manutencao.listarAtivosHidricos();
        return new AguaDashboardResumo(lista.stream().filter(RecursoHidricoOperacionalResumo::ativo).count(),
                lista.stream().filter(RecursoHidricoOperacionalResumo::volumeConhecido).count(),
                lista.stream().filter(RecursoHidricoOperacionalResumo::nivelBaixo).count(),
                lista.stream().filter(RecursoHidricoOperacionalResumo::inspecaoVencida).count(),
                ativos.size(), manutencao.contarManutencoesHidricasVencidas(), lista,
                listarOperacoes(0, 10).conteudo(), ativos, manutencao.listarManutencoesHidricas());
    }

    @Transactional(readOnly = true)
    public RecursoHidricoOperacaoConfigRequest formularioConfiguracao(Long id) {
        RecursoHidrico recurso = buscar(id);
        RecursoHidricoOperacaoConfigRequest request = new RecursoHidricoOperacaoConfigRequest();
        request.setNivelMinimoAlertaLitros(recurso.getNivelMinimoAlertaLitros());
        request.setIntervaloInspecaoDias(recurso.getIntervaloInspecaoDias());
        request.setProximaInspecao(recurso.getProximaInspecao());
        request.setVersao(recurso.getVersao());
        return request;
    }

    @Transactional
    public RecursoHidricoOperacionalResumo configurar(Long id,
            RecursoHidricoOperacaoConfigRequest request, UsuarioAtor ator) {
        RecursoHidrico recurso = bloquear(id);
        if (request.getVersao() == null || request.getVersao() != recurso.getVersao())
            throw erro("VERSAO_DESATUALIZADA", "O recurso foi alterado. Recarregue a página.", HttpStatus.CONFLICT);
        validarConfiguracao(recurso, request);
        LocalDateTime anterior = recurso.getProximaInspecao();
        recurso.setNivelMinimoAlertaLitros(decimal(request.getNivelMinimoAlertaLitros()));
        recurso.setIntervaloInspecaoDias(request.getIntervaloInspecaoDias());
        recurso.setProximaInspecao(request.getProximaInspecao());
        recursos.flush();
        if (anterior != null && !anterior.equals(recurso.getProximaInspecao()))
            tarefas.concluirAutomatica(chaveInspecao(recurso.getId(), anterior), ator);
        sincronizarTarefaInspecao(recurso, ator);
        alertas.avaliar();
        return resumo(recurso);
    }

    @Transactional
    public OperacaoAguaResumo registrar(OperacaoAguaRequest request, UsuarioAtor ator) {
        validarRequest(request);
        String chave = request.getChaveIdempotencia().trim();
        locks.bloquearIdempotencia("AGUA_OPERACAO", chave);
        OperacaoAgua existente = operacoes.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            if (!mesmaOperacao(existente, request))
                throw erro("CHAVE_IDEMPOTENCIA_DIVERGENTE", "A chave já pertence a outra operação.", HttpStatus.CONFLICT);
            return resumo(existente);
        }

        RecursoHidrico origem;
        RecursoHidrico destino = null;
        if (request.getTipo() == TipoOperacaoAgua.TRANSFERENCIA
                && request.getRecursoDestinoId() < request.getRecursoOrigemId()) {
            destino = bloquear(request.getRecursoDestinoId());
            origem = bloquear(request.getRecursoOrigemId());
        } else {
            origem = bloquear(request.getRecursoOrigemId());
            if (request.getRecursoDestinoId() != null) destino = bloquear(request.getRecursoDestinoId());
        }
        validarAtivo(origem);
        if (destino != null) validarAtivo(destino);

        OperacaoAgua operacao = new OperacaoAgua();
        operacao.setRecursoOrigem(origem);
        operacao.setRecursoDestino(destino);
        operacao.setTipo(request.getTipo());
        operacao.setDataOperacao(request.getDataOperacao());
        operacao.setQuantidadeLitros(decimal(request.getQuantidadeLitros()));
        operacao.setObservacao(texto(request.getObservacao()));
        operacao.setChaveIdempotencia(chave);
        aplicar(operacao, origem, destino, ator);
        operacao = operacoes.saveAndFlush(operacao);
        recursos.flush();
        sincronizarTarefaInspecao(origem, ator);
        alertas.avaliar();
        return resumo(operacao);
    }

    private void aplicar(OperacaoAgua operacao, RecursoHidrico origem, RecursoHidrico destino, UsuarioAtor ator) {
        BigDecimal quantidade = operacao.getQuantidadeLitros();
        operacao.setVolumeOrigemAntesLitros(origem.getVolumeAtualLitros());
        switch (operacao.getTipo()) {
            case MEDICAO_NIVEL -> origem.setVolumeAtualLitros(validarCapacidade(origem, quantidade));
            case ABASTECIMENTO -> origem.setVolumeAtualLitros(validarCapacidade(origem,
                    volumeConhecido(origem).add(quantidade)));
            case CONSUMO, RETIRADA -> origem.setVolumeAtualLitros(subtrair(origem, quantidade));
            case TRANSFERENCIA -> {
                operacao.setVolumeDestinoAntesLitros(volumeConhecido(destino));
                origem.setVolumeAtualLitros(subtrair(origem, quantidade));
                destino.setVolumeAtualLitros(validarCapacidade(destino,
                        operacao.getVolumeDestinoAntesLitros().add(quantidade)));
                operacao.setVolumeDestinoDepoisLitros(destino.getVolumeAtualLitros());
            }
            case INSPECAO -> {
                LocalDateTime anterior = origem.getProximaInspecao();
                if (anterior != null) tarefas.concluirAutomatica(chaveInspecao(origem.getId(), anterior), ator);
                origem.setUltimaInspecao(operacao.getDataOperacao());
                origem.setProximaInspecao(operacao.getDataOperacao().plusDays(origem.getIntervaloInspecaoDias()));
            }
        }
        operacao.setVolumeOrigemDepoisLitros(origem.getVolumeAtualLitros());
    }

    private void validarRequest(OperacaoAguaRequest r) {
        if (r.getRecursoOrigemId() == null) throw erro("RECURSO_OBRIGATORIO", "Informe o recurso hídrico.");
        if (r.getTipo() == null) throw erro("TIPO_OPERACAO_OBRIGATORIO", "Informe o tipo da operação.");
        if (r.getDataOperacao() == null || r.getDataOperacao().isAfter(LocalDateTime.now(clock).plusMinutes(1)))
            throw erro("DATA_OPERACAO_INVALIDA", "A data da operação é obrigatória e não pode estar no futuro.");
        if (!StringUtils.hasText(r.getChaveIdempotencia()))
            throw erro("CHAVE_IDEMPOTENCIA_OBRIGATORIA", "A chave de idempotência é obrigatória.");
        if (r.getTipo() == TipoOperacaoAgua.INSPECAO && r.getQuantidadeLitros() != null)
            throw erro("QUANTIDADE_INDEVIDA", "Inspeção não deve informar volume.");
        if (r.getTipo() == TipoOperacaoAgua.MEDICAO_NIVEL
                && (r.getQuantidadeLitros() == null || r.getQuantidadeLitros().signum() < 0))
            throw erro("VOLUME_INVALIDO", "A medição deve informar volume maior ou igual a zero.");
        if (r.getTipo() != TipoOperacaoAgua.MEDICAO_NIVEL && r.getTipo() != TipoOperacaoAgua.INSPECAO
                && (r.getQuantidadeLitros() == null || r.getQuantidadeLitros().signum() <= 0))
            throw erro("VOLUME_INVALIDO", "Informe volume maior que zero.");
        if (r.getTipo() == TipoOperacaoAgua.TRANSFERENCIA) {
            if (r.getRecursoDestinoId() == null || Objects.equals(r.getRecursoDestinoId(), r.getRecursoOrigemId()))
                throw erro("DESTINO_INVALIDO", "Informe outro recurso como destino da transferência.");
        } else if (r.getRecursoDestinoId() != null) {
            throw erro("DESTINO_INDEVIDO", "Destino é permitido somente em transferências.");
        }
    }

    private void validarConfiguracao(RecursoHidrico recurso, RecursoHidricoOperacaoConfigRequest request) {
        if (request.getIntervaloInspecaoDias() < 1 || request.getIntervaloInspecaoDias() > 365)
            throw erro("INTERVALO_INSPECAO_INVALIDO", "O intervalo deve estar entre 1 e 365 dias.");
        if (request.getProximaInspecao() == null)
            throw erro("PROXIMA_INSPECAO_OBRIGATORIA", "Informe a próxima inspeção.");
        BigDecimal limite = request.getNivelMinimoAlertaLitros();
        if (limite != null && limite.signum() < 0)
            throw erro("NIVEL_MINIMO_INVALIDO", "O nível mínimo não pode ser negativo.");
        if (limite != null && recurso.getCapacidadeLitros() != null
                && limite.compareTo(recurso.getCapacidadeLitros()) > 0)
            throw erro("NIVEL_MINIMO_INVALIDO", "O nível mínimo não pode exceder a capacidade.");
    }

    private void sincronizarTarefaInspecao(RecursoHidrico recurso, UsuarioAtor ator) {
        if (!recurso.isAtivo() || recurso.getProximaInspecao() == null) return;
        tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(
                chaveInspecao(recurso.getId(), recurso.getProximaInspecao()),
                "Inspecionar " + recurso.getNome(),
                "Verificar nível, integridade e condições operacionais do recurso hídrico.",
                recurso.getProximaInspecao().isBefore(LocalDateTime.now(clock))
                        ? PrioridadeTarefa.ALTA : PrioridadeTarefa.NORMAL,
                recurso.getProximaInspecao(), ModuloOrigem.AGUA, referencia(recurso.getId())), ator);
    }

    private RecursoHidricoOperacionalResumo resumo(RecursoHidrico recurso) {
        BigDecimal percentual = recurso.getVolumeAtualLitros() == null || recurso.getCapacidadeLitros() == null
                ? null : recurso.getVolumeAtualLitros().multiply(new BigDecimal("100"))
                        .divide(recurso.getCapacidadeLitros(), 1, RoundingMode.HALF_UP);
        return new RecursoHidricoOperacionalResumo(recurso.getId(), recurso.getNome(), recurso.getTipo(), recurso.isAtivo(),
                recurso.getCapacidadeLitros(), recurso.getVolumeAtualLitros(), percentual,
                alertas.limiteEfetivo(recurso), alertas.nivelBaixo(recurso), recurso.getVolumeAtualLitros() != null,
                recurso.getIntervaloInspecaoDias(), recurso.getUltimaInspecao(), recurso.getProximaInspecao(),
                recurso.getProximaInspecao() != null && recurso.getProximaInspecao().isBefore(LocalDateTime.now(clock)),
                recurso.getObservacao(), recurso.getVersao());
    }

    private OperacaoAguaResumo resumo(OperacaoAgua operacao) {
        return new OperacaoAguaResumo(operacao.getId(), operacao.getTipo(), operacao.getDataOperacao(),
                operacao.getRecursoOrigem().getId(), operacao.getRecursoOrigem().getNome(),
                operacao.getRecursoDestino() == null ? null : operacao.getRecursoDestino().getId(),
                operacao.getRecursoDestino() == null ? null : operacao.getRecursoDestino().getNome(),
                operacao.getQuantidadeLitros(), operacao.getVolumeOrigemAntesLitros(),
                operacao.getVolumeOrigemDepoisLitros(), operacao.getVolumeDestinoAntesLitros(),
                operacao.getVolumeDestinoDepoisLitros(), operacao.getObservacao(), operacao.getVersao(),
                operacao.getCriadoEm(), operacao.getCriadoPor());
    }

    private BigDecimal validarCapacidade(RecursoHidrico recurso, BigDecimal volume) {
        BigDecimal normalizado = decimal(volume);
        if (normalizado.signum() < 0) throw erro("SALDO_INSUFICIENTE", "O volume não pode ficar negativo.");
        if (recurso.getCapacidadeLitros() != null && normalizado.compareTo(recurso.getCapacidadeLitros()) > 0)
            throw erro("CAPACIDADE_EXCEDIDA", "O volume excede a capacidade de " + recurso.getNome() + ".");
        return normalizado;
    }

    private BigDecimal subtrair(RecursoHidrico recurso, BigDecimal quantidade) {
        return validarCapacidade(recurso, volumeConhecido(recurso).subtract(quantidade));
    }

    private BigDecimal volumeConhecido(RecursoHidrico recurso) {
        if (recurso == null || recurso.getVolumeAtualLitros() == null)
            throw erro("VOLUME_DESCONHECIDO", "Registre uma medição de nível antes de movimentar água.");
        return recurso.getVolumeAtualLitros();
    }

    private boolean mesmaOperacao(OperacaoAgua existente, OperacaoAguaRequest request) {
        Long destinoId = existente.getRecursoDestino() == null ? null : existente.getRecursoDestino().getId();
        return Objects.equals(existente.getRecursoOrigem().getId(), request.getRecursoOrigemId())
                && Objects.equals(destinoId, request.getRecursoDestinoId())
                && existente.getTipo() == request.getTipo()
                && Objects.equals(existente.getDataOperacao(), request.getDataOperacao())
                && decimalIgual(existente.getQuantidadeLitros(), request.getQuantidadeLitros())
                && Objects.equals(existente.getObservacao(), texto(request.getObservacao()));
    }

    private boolean decimalIgual(BigDecimal atual, BigDecimal informado) {
        BigDecimal normalizado = decimal(informado);
        return atual == null ? normalizado == null : normalizado != null && atual.compareTo(normalizado) == 0;
    }

    private void validarAtivo(RecursoHidrico recurso) {
        if (!recurso.isAtivo()) throw erro("RECURSO_INATIVO", "O recurso hídrico está inativo.");
    }

    private RecursoHidrico buscar(Long id) {
        return recursos.findByIdAndPropriedadeId(id, propriedadeId())
                .orElseThrow(() -> erro("RECURSO_NAO_ENCONTRADO", "Recurso hídrico não encontrado.", HttpStatus.NOT_FOUND));
    }

    private RecursoHidrico bloquear(Long id) {
        return recursos.buscarParaAtualizacao(id, propriedadeId())
                .orElseThrow(() -> erro("RECURSO_NAO_ENCONTRADO", "Recurso hídrico não encontrado.", HttpStatus.NOT_FOUND));
    }

    private Long propriedadeId() {
        return propriedades.findByPrincipalTrue()
                .orElseThrow(() -> erro("PROPRIEDADE_AUSENTE", "Propriedade principal não configurada."))
                .getId();
    }

    private int limitarPagina(int pagina) { return Math.max(0, pagina); }
    private int limitarTamanho(int tamanho) { return Math.max(1, Math.min(tamanho, 100)); }
    private BigDecimal decimal(BigDecimal valor) { return valor == null ? null : valor.setScale(3, RoundingMode.HALF_UP); }
    private String texto(String valor) { return StringUtils.hasText(valor) ? valor.trim() : null; }
    public static String referencia(Long recursoId) { return "RECURSO_HIDRICO:" + recursoId; }
    public static String chaveInspecao(Long recursoId, LocalDateTime data) {
        return "AGUA:RECURSO:" + recursoId + ":INSPECAO:" + data.toLocalDate();
    }
    private AguaOperacaoException erro(String codigo, String mensagem) { return new AguaOperacaoException(codigo, mensagem); }
    private AguaOperacaoException erro(String codigo, String mensagem, HttpStatus status) {
        return new AguaOperacaoException(codigo, mensagem, status);
    }
}
