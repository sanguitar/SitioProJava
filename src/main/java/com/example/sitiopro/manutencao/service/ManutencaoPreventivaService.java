package com.example.sitiopro.manutencao.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.*;
import com.example.sitiopro.tarefas.dto.TarefaAutomaticaRequest;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
public class ManutencaoPreventivaService {
    private final PlanoManutencaoPreventivaRepository planos;
    private final LeituraMedidorAtivoRepository leituras;
    private final AtivoPatrimonialRepository ativos;
    private final CodigoCriacaoService codigos;
    private final TarefaService tarefas;
    private final ManutencaoAlertasService alertas;
    private final Clock clock;

    public ManutencaoPreventivaService(PlanoManutencaoPreventivaRepository planos,
            LeituraMedidorAtivoRepository leituras, AtivoPatrimonialRepository ativos,
            CodigoCriacaoService codigos, TarefaService tarefas,
            ManutencaoAlertasService alertas, Clock clock) {
        this.planos = planos; this.leituras = leituras; this.ativos = ativos;
        this.codigos = codigos; this.tarefas = tarefas; this.alertas = alertas; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<PlanoManutencaoPreventivaResumo> listarPlanos(Long ativoId) {
        return planos.findByAtivoPatrimonialIdOrderByAtivoDescNomeAsc(ativoId).stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public List<PlanoManutencaoPreventivaResumo> listarPlanosAtivos(Long ativoId) {
        return listarPlanos(ativoId).stream().filter(PlanoManutencaoPreventivaResumo::ativo).toList();
    }

    @Transactional(readOnly = true)
    public List<LeituraMedidorResumo> listarLeituras(Long ativoId) {
        return leituras.findByAtivoPatrimonialIdOrderByDataLeituraDescIdDesc(ativoId).stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public PlanoManutencaoPreventivaRequest formularioPlano(Long id) {
        PlanoManutencaoPreventiva p = buscarPlano(id);
        PlanoManutencaoPreventivaRequest r = new PlanoManutencaoPreventivaRequest();
        r.setAtivoId(p.getAtivoPatrimonial().getId()); r.setNome(p.getNome()); r.setDescricao(p.getDescricao());
        r.setTipoPeriodicidade(p.getTipoPeriodicidade()); r.setIntervalo(p.getIntervalo());
        r.setDataReferencia(p.getDataReferencia()); r.setChaveIdempotencia(p.getChaveIdempotencia()); r.setVersao(p.getVersao());
        return r;
    }

    @Transactional
    public PlanoManutencaoPreventivaResumo criarPlano(PlanoManutencaoPreventivaRequest request, UsuarioAtor ator) {
        validarPlano(request);
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigos.bloquearIdempotencia("MANUTENCAO_PLANO", chave);
        PlanoManutencaoPreventiva existente = planos.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) return resumo(existente);
        AtivoPatrimonial ativo = buscarAtivo(request.getAtivoId(), true);
        PlanoManutencaoPreventiva plano = new PlanoManutencaoPreventiva();
        plano.setAtivoPatrimonial(ativo); plano.setChaveIdempotencia(chave);
        preencher(plano, request, false);
        plano = planos.saveAndFlush(plano);
        sincronizarTarefa(plano, ator); alertas.avaliar();
        return resumo(plano);
    }

    @Transactional
    public PlanoManutencaoPreventivaResumo atualizarPlano(Long id,
            PlanoManutencaoPreventivaRequest request, UsuarioAtor ator) {
        validarPlano(request);
        PlanoManutencaoPreventiva plano = planos.buscarParaAtualizacao(id).orElseThrow(() -> planoAusente(id));
        if (!Objects.equals(plano.getAtivoPatrimonial().getId(), request.getAtivoId()))
            throw erro("ATIVO_PLANO_IMUTAVEL", "O ativo do plano não pode ser alterado.");
        if (request.getVersao() == null || request.getVersao() != plano.getVersao())
            throw erro("VERSAO_DESATUALIZADA", "O plano foi alterado por outro usuário. Recarregue a página.", HttpStatus.CONFLICT);
        preencher(plano, request, true); planos.flush(); sincronizarTarefa(plano, ator); alertas.avaliar();
        return resumo(plano);
    }

    @Transactional
    public PlanoManutencaoPreventivaResumo desativarPlano(Long id, long versao, UsuarioAtor ator) {
        PlanoManutencaoPreventiva plano = planos.buscarParaAtualizacao(id).orElseThrow(() -> planoAusente(id));
        if (plano.getVersao() != versao)
            throw erro("VERSAO_DESATUALIZADA", "O plano foi alterado por outro usuário. Recarregue a página.", HttpStatus.CONFLICT);
        if (plano.isAtivo()) {
            plano.setAtivo(false);
            tarefas.concluirAutomatica(chaveTarefa(plano), ator);
            planos.flush(); alertas.avaliar();
        }
        return resumo(plano);
    }

    @Transactional
    public LeituraMedidorResumo registrarLeitura(LeituraMedidorRequest request, UsuarioAtor ator) {
        return registrarLeitura(request, ator, false);
    }

    @Transactional
    public LeituraMedidorResumo registrarAjusteAdministrativo(LeituraMedidorRequest request, UsuarioAtor ator) {
        if (!ator.admin()) throw erro("AJUSTE_REQUER_ADMIN", "Somente administradores podem corrigir uma leitura.", HttpStatus.FORBIDDEN);
        return registrarLeitura(request, ator, true);
    }

    private LeituraMedidorResumo registrarLeitura(LeituraMedidorRequest request, UsuarioAtor ator, boolean ajuste) {
        validarLeitura(request, ajuste);
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigos.bloquearIdempotencia("MANUTENCAO_LEITURA", chave);
        LeituraMedidorAtivo existente = leituras.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            if (!Objects.equals(existente.getAtivoPatrimonial().getId(), request.getAtivoId()))
                throw erro("CHAVE_IDEMPOTENCIA_DIVERGENTE", "A chave já pertence a outro ativo.");
            return resumo(existente);
        }
        AtivoPatrimonial ativo = buscarAtivo(request.getAtivoId(), true);
        validarSequencia(ativo.getId(), request, ajuste);
        LeituraMedidorAtivo leitura = new LeituraMedidorAtivo();
        leitura.setAtivoPatrimonial(ativo); leitura.setDataLeitura(request.getDataLeitura());
        leitura.setHorimetro(decimal(request.getHorimetro())); leitura.setQuilometragem(decimal(request.getQuilometragem()));
        leitura.setTipoLeitura(ajuste ? TipoLeituraMedidor.AJUSTE_ADMINISTRATIVO : TipoLeituraMedidor.OPERACIONAL);
        leitura.setJustificativaAjuste(ajuste ? obrigatorio(request.getJustificativaAjuste(), "Justificativa") : null);
        leitura.setChaveIdempotencia(chave); leitura = leituras.saveAndFlush(leitura);
        sincronizarPlanosDoAtivo(ativo.getId(), ator); alertas.avaliar();
        return resumo(leitura);
    }

    @Transactional
    public void concluirCiclo(Long planoId, Long ativoId, LocalDateTime dataManutencao,
            BigDecimal horimetro, BigDecimal quilometragem, UsuarioAtor ator) {
        if (planoId == null) return;
        PlanoManutencaoPreventiva plano = planos.buscarParaAtualizacao(planoId).orElseThrow(() -> planoAusente(planoId));
        if (!plano.isAtivo() || !Objects.equals(plano.getAtivoPatrimonial().getId(), ativoId))
            throw erro("PLANO_PREVENTIVO_INVALIDO", "O plano preventivo não está ativo para o ativo informado.");
        tarefas.concluirAutomatica(chaveTarefa(plano), ator);
        switch (plano.getTipoPeriodicidade()) {
            case DIAS -> {
                plano.setDataReferencia(dataManutencao);
                plano.setProximaData(dataManutencao.plusDays(dias(plano.getIntervalo())));
            }
            case HORIMETRO -> {
                BigDecimal base = horimetro != null ? decimal(horimetro) : leituraAtual(ativoId, TipoPeriodicidadeManutencao.HORIMETRO);
                if (base == null) throw erro("LEITURA_HORIMETRO_AUSENTE", "Informe o horímetro ou registre uma leitura antes de concluir o plano.");
                plano.setValorReferencia(base); plano.setProximoValor(base.add(plano.getIntervalo()));
            }
            case QUILOMETRAGEM -> {
                BigDecimal base = quilometragem != null ? decimal(quilometragem) : leituraAtual(ativoId, TipoPeriodicidadeManutencao.QUILOMETRAGEM);
                if (base == null) throw erro("LEITURA_QUILOMETRAGEM_AUSENTE", "Informe a quilometragem ou registre uma leitura antes de concluir o plano.");
                plano.setValorReferencia(base); plano.setProximoValor(base.add(plano.getIntervalo()));
            }
        }
        plano.setCicloAtual(plano.getCicloAtual() + 1); planos.flush();
        sincronizarTarefa(plano, ator);
    }

    @Transactional(readOnly = true)
    public PlanoManutencaoPreventiva buscarPlanoAtivoDoAtivo(Long planoId, Long ativoId) {
        if (planoId == null) return null;
        PlanoManutencaoPreventiva p = buscarPlano(planoId);
        if (!p.isAtivo() || !Objects.equals(p.getAtivoPatrimonial().getId(), ativoId))
            throw erro("PLANO_PREVENTIVO_INVALIDO", "O plano preventivo não está ativo para o ativo informado.");
        return p;
    }

    @Transactional(readOnly = true)
    public long contarPlanosAtivos() { return planos.countByAtivoTrue(); }

    @Transactional(readOnly = true)
    public long contarPlanosVencidos() {
        return planos.findByAtivoTrueOrderByProximaDataAscNomeAsc().stream().filter(this::vencido).count();
    }

    @Transactional(readOnly = true)
    public long contarPlanosProximos() {
        return planos.findByAtivoTrueOrderByProximaDataAscNomeAsc().stream().filter(p -> !vencido(p)).count();
    }

    @Transactional(readOnly = true)
    public List<PlanoManutencaoPreventivaResumo> planosPrioritarios() {
        return planos.findByAtivoTrueOrderByProximaDataAscNomeAsc().stream().map(this::resumo)
                .sorted(Comparator.comparing(PlanoManutencaoPreventivaResumo::vencido).reversed()
                        .thenComparing(PlanoManutencaoPreventivaResumo::nome))
                .limit(10).toList();
    }

    private void preencher(PlanoManutencaoPreventiva plano, PlanoManutencaoPreventivaRequest r, boolean edicao) {
        plano.setNome(obrigatorio(r.getNome(), "Nome")); plano.setDescricao(texto(r.getDescricao()));
        plano.setTipoPeriodicidade(r.getTipoPeriodicidade()); plano.setIntervalo(decimal(r.getIntervalo()));
        plano.setAtivo(true);
        switch (r.getTipoPeriodicidade()) {
            case DIAS -> {
                LocalDateTime referencia = r.getDataReferencia() == null ? LocalDateTime.now(clock) : r.getDataReferencia();
                plano.setDataReferencia(referencia); plano.setProximaData(referencia.plusDays(dias(plano.getIntervalo())));
                plano.setValorReferencia(null); plano.setProximoValor(null);
            }
            case HORIMETRO, QUILOMETRAGEM -> {
                BigDecimal atual = leituraAtual(plano.getAtivoPatrimonial().getId(), r.getTipoPeriodicidade());
                if (atual == null) throw erro("LEITURA_INICIAL_AUSENTE", "Registre a leitura do medidor antes de criar este plano.");
                plano.setDataReferencia(null); plano.setProximaData(null);
                plano.setValorReferencia(atual); plano.setProximoValor(atual.add(plano.getIntervalo()));
            }
        }
        if (!edicao) plano.setCicloAtual(1);
    }

    private void validarPlano(PlanoManutencaoPreventivaRequest r) {
        if (r.getAtivoId() == null) throw erro("ATIVO_OBRIGATORIO", "Informe o ativo.");
        if (r.getTipoPeriodicidade() == null) throw erro("PERIODICIDADE_OBRIGATORIA", "Informe a periodicidade.");
        if (r.getIntervalo() == null || r.getIntervalo().signum() <= 0) throw erro("INTERVALO_INVALIDO", "O intervalo deve ser maior que zero.");
        if (r.getTipoPeriodicidade() == TipoPeriodicidadeManutencao.DIAS && r.getIntervalo().stripTrailingZeros().scale() > 0)
            throw erro("INTERVALO_DIAS_INVALIDO", "A periodicidade em dias deve usar um número inteiro.");
        if (r.getDataReferencia() != null && r.getDataReferencia().isAfter(LocalDateTime.now(clock).plusMinutes(1)))
            throw erro("DATA_REFERENCIA_INVALIDA", "A data de referência não pode estar no futuro.");
    }

    private void validarLeitura(LeituraMedidorRequest r, boolean ajuste) {
        if (r.getAtivoId() == null) throw erro("ATIVO_OBRIGATORIO", "Informe o ativo.");
        if (r.getDataLeitura() == null || r.getDataLeitura().isAfter(LocalDateTime.now(clock).plusMinutes(1)))
            throw erro("DATA_LEITURA_INVALIDA", "A data da leitura é obrigatória e não pode estar no futuro.");
        if (r.getHorimetro() == null && r.getQuilometragem() == null)
            throw erro("LEITURA_VAZIA", "Informe o horímetro ou a quilometragem.");
        if (r.getHorimetro() != null && r.getHorimetro().signum() < 0 || r.getQuilometragem() != null && r.getQuilometragem().signum() < 0)
            throw erro("LEITURA_INVALIDA", "As leituras não podem ser negativas.");
        if (ajuste && !StringUtils.hasText(r.getJustificativaAjuste()))
            throw erro("JUSTIFICATIVA_OBRIGATORIA", "Justifique o ajuste administrativo.");
    }

    private void validarSequencia(Long ativoId, LeituraMedidorRequest r, boolean ajuste) {
        leituras.findTopByAtivoPatrimonialIdOrderByDataLeituraDescIdDesc(ativoId).ifPresent(ultima -> {
            if (r.getDataLeitura().isBefore(ultima.getDataLeitura()))
                throw erro("DATA_LEITURA_REGRESSIVA", "A data da leitura não pode preceder a leitura mais recente.");
        });
        if (ajuste) return;
        if (r.getHorimetro() != null) leituras.findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(ativoId).ifPresent(ultima -> {
            if (r.getHorimetro().compareTo(ultima.getHorimetro()) < 0)
                throw erro("HORIMETRO_REGRESSIVO", "O horímetro não pode regredir. Use o ajuste administrativo para correções.");
        });
        if (r.getQuilometragem() != null) leituras.findTopByAtivoPatrimonialIdAndQuilometragemIsNotNullOrderByDataLeituraDescIdDesc(ativoId).ifPresent(ultima -> {
            if (r.getQuilometragem().compareTo(ultima.getQuilometragem()) < 0)
                throw erro("QUILOMETRAGEM_REGRESSIVA", "A quilometragem não pode regredir. Use o ajuste administrativo para correções.");
        });
    }

    private void sincronizarPlanosDoAtivo(Long ativoId, UsuarioAtor ator) {
        planos.findByAtivoPatrimonialIdOrderByAtivoDescNomeAsc(ativoId).stream().filter(PlanoManutencaoPreventiva::isAtivo)
                .forEach(p -> sincronizarTarefa(p, ator));
    }

    private void sincronizarTarefa(PlanoManutencaoPreventiva p, UsuarioAtor ator) {
        BigDecimal atual = leituraAtual(p.getAtivoPatrimonial().getId(), p.getTipoPeriodicidade());
        boolean vencido = vencido(p, atual);
        String limite = p.getTipoPeriodicidade() == TipoPeriodicidadeManutencao.DIAS
                ? "até " + p.getProximaData()
                : "em " + p.getProximoValor() + " " + unidade(p.getTipoPeriodicidade());
        tarefas.sincronizarAutomatica(new TarefaAutomaticaRequest(chaveTarefa(p),
                "Preventiva: " + p.getAtivoPatrimonial().getCodigo(),
                p.getNome() + " · próxima intervenção " + limite + ".",
                vencido ? PrioridadeTarefa.CRITICA : PrioridadeTarefa.ALTA,
                p.getProximaData(), ModuloOrigem.MANUTENCAO,
                ManutencaoService.referencia(p.getAtivoPatrimonial().getId())), ator);
    }

    private BigDecimal leituraAtual(Long ativoId, TipoPeriodicidadeManutencao tipo) {
        return switch (tipo) {
            case DIAS -> null;
            case HORIMETRO -> leituras.findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(ativoId)
                    .map(LeituraMedidorAtivo::getHorimetro).orElse(null);
            case QUILOMETRAGEM -> leituras.findTopByAtivoPatrimonialIdAndQuilometragemIsNotNullOrderByDataLeituraDescIdDesc(ativoId)
                    .map(LeituraMedidorAtivo::getQuilometragem).orElse(null);
        };
    }

    public boolean vencido(PlanoManutencaoPreventiva p) {
        return vencido(p, leituraAtual(p.getAtivoPatrimonial().getId(), p.getTipoPeriodicidade()));
    }

    private boolean vencido(PlanoManutencaoPreventiva p, BigDecimal atual) {
        if (!p.isAtivo()) return false;
        return p.getTipoPeriodicidade() == TipoPeriodicidadeManutencao.DIAS
                ? p.getProximaData().isBefore(LocalDateTime.now(clock))
                : atual != null && atual.compareTo(p.getProximoValor()) >= 0;
    }

    private PlanoManutencaoPreventivaResumo resumo(PlanoManutencaoPreventiva p) {
        BigDecimal atual = leituraAtual(p.getAtivoPatrimonial().getId(), p.getTipoPeriodicidade());
        return new PlanoManutencaoPreventivaResumo(p.getId(), p.getAtivoPatrimonial().getId(),
                p.getAtivoPatrimonial().getCodigo(), p.getAtivoPatrimonial().getNome(), p.getNome(), p.getDescricao(),
                p.getTipoPeriodicidade(), p.getIntervalo(), p.getProximaData(), p.getProximoValor(), atual,
                p.isAtivo(), vencido(p, atual), p.getCicloAtual(), p.getVersao());
    }

    private LeituraMedidorResumo resumo(LeituraMedidorAtivo l) {
        return new LeituraMedidorResumo(l.getId(), l.getAtivoPatrimonial().getId(), l.getDataLeitura(),
                l.getHorimetro(), l.getQuilometragem(), l.getTipoLeitura(), l.getJustificativaAjuste(),
                l.getVersao(), l.getCriadoEm(), l.getCriadoPor());
    }

    public static String chaveTarefa(PlanoManutencaoPreventiva p) {
        return "MANUTENCAO:PLANO:" + p.getId() + ":CICLO:" + p.getCicloAtual();
    }
    public static String chaveAlerta(PlanoManutencaoPreventiva p) { return "MANUTENCAO:PLANO:" + p.getId() + ":VENCIDA"; }
    private String unidade(TipoPeriodicidadeManutencao tipo) { return tipo == TipoPeriodicidadeManutencao.HORIMETRO ? "h" : "km"; }
    private long dias(BigDecimal valor) { try { return valor.longValueExact(); } catch (ArithmeticException ex) { throw erro("INTERVALO_DIAS_INVALIDO", "A periodicidade em dias deve usar um número inteiro."); } }
    private AtivoPatrimonial buscarAtivo(Long id, boolean bloquear) { return (bloquear ? ativos.buscarParaAtualizacao(id) : ativos.findById(id)).orElseThrow(() -> erro("ATIVO_NAO_ENCONTRADO", "Ativo não encontrado: " + id, HttpStatus.NOT_FOUND)); }
    private PlanoManutencaoPreventiva buscarPlano(Long id) { return planos.findById(id).orElseThrow(() -> planoAusente(id)); }
    private ManutencaoOperacaoException planoAusente(Long id) { return erro("PLANO_NAO_ENCONTRADO", "Plano preventivo não encontrado: " + id, HttpStatus.NOT_FOUND); }
    private String obrigatorio(String v, String campo) { if (!StringUtils.hasText(v)) throw erro("CAMPO_OBRIGATORIO", campo + " é obrigatório."); return v.trim(); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private BigDecimal decimal(BigDecimal v) { return v == null ? null : v.setScale(2, RoundingMode.HALF_UP); }
    private ManutencaoOperacaoException erro(String c, String m) { return new ManutencaoOperacaoException(c, m); }
    private ManutencaoOperacaoException erro(String c, String m, HttpStatus s) { return new ManutencaoOperacaoException(c, m, s); }
}
