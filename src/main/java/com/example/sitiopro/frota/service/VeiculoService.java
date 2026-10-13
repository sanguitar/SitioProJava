package com.example.sitiopro.frota.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.frota.dto.*;
import com.example.sitiopro.frota.model.*;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.AtivoPatrimonialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.*;
import java.util.*;

@Service
public class VeiculoService {
    private static final Set<TipoAtivoPatrimonial> TIPOS_VINCULAVEIS = EnumSet.of(
            TipoAtivoPatrimonial.MAQUINA, TipoAtivoPatrimonial.EQUIPAMENTO,
            TipoAtivoPatrimonial.IMPLEMENTO, TipoAtivoPatrimonial.OUTRO);
    private final VeiculoRepository veiculos;
    private final AtivoPatrimonialRepository ativos;
    private final CodigoCriacaoService codigos;
    private final FipeService fipe;

    public VeiculoService(VeiculoRepository veiculos, AtivoPatrimonialRepository ativos,
            CodigoCriacaoService codigos, FipeService fipe) {
        this.veiculos = veiculos; this.ativos = ativos; this.codigos = codigos; this.fipe = fipe;
    }

    @Transactional(readOnly = true)
    public List<VeiculoResumo> listarTodos() {
        return veiculos.findAllByOrderByNomeAsc().stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResumo detalhar(Long id) { return resumo(buscarPorId(id)); }

    @Transactional(readOnly = true)
    public VeiculoRequest formulario(Long id) {
        Veiculo v = buscarPorId(id);
        VeiculoRequest r = new VeiculoRequest();
        r.setNome(v.getNome()); r.setPlaca(v.getPlaca()); r.setTipo(v.getTipo());
        r.setMarcaFipe(v.getMarcaFipe()); r.setModeloFipe(v.getModeloFipe()); r.setAnoModelo(v.getAnoModelo());
        r.setKmAtual(v.getKmAtual()); r.setTipoMedidor(v.getTipoMedidor()); r.setHorimetroAtual(v.getHorimetroAtual());
        r.setValorFipe(v.getValorFipe()); r.setUltimaConsultaFipe(v.getUltimaConsultaFipe());
        r.setAtivoPatrimonialId(v.getAtivoPatrimonial() == null ? null : v.getAtivoPatrimonial().getId());
        r.setChaveIdempotencia(v.getChaveIdempotencia()); r.setVersao(v.getVersao());
        return r;
    }

    @Transactional(readOnly = true)
    public List<AtivoFrotaResumo> listarAtivosVinculaveis(Long veiculoId) {
        return ativos.findByStatusInOrderByNomeAsc(List.of(StatusAtivoPatrimonial.ATIVO, StatusAtivoPatrimonial.EM_MANUTENCAO))
                .stream().filter(a -> TIPOS_VINCULAVEIS.contains(a.getTipo()))
                .filter(a -> !vinculadoAOutro(a.getId(), veiculoId))
                .map(a -> new AtivoFrotaResumo(a.getId(), a.getCodigo(), a.getNome(), a.getTipo().getRotulo())).toList();
    }

    @Transactional
    public VeiculoResumo criar(VeiculoRequest request) {
        validar(request, false);
        String chave = obrigatorio(request.getChaveIdempotencia(), "Chave de idempotência");
        codigos.bloquearIdempotencia("FROTA_VEICULO", chave);
        Veiculo existente = veiculos.findByChaveIdempotencia(chave).orElse(null);
        if (existente != null) {
            if (!Objects.equals(existente.getNome(), request.getNome().trim().toUpperCase(Locale.ROOT)))
                throw erro("CHAVE_IDEMPOTENCIA_DIVERGENTE", "A chave já pertence a outro veículo.");
            return resumo(existente);
        }
        Veiculo veiculo = new Veiculo(); veiculo.setChaveIdempotencia(chave);
        preencher(veiculo, request, false);
        return resumo(veiculos.saveAndFlush(veiculo));
    }

    @Transactional
    public VeiculoResumo atualizar(Long id, VeiculoRequest request) {
        validar(request, true);
        Veiculo veiculo = buscarParaAtualizacao(id);
        if (request.getVersao() == null || request.getVersao() != veiculo.getVersao())
            throw erro("VERSAO_DESATUALIZADA", "O veículo foi alterado por outro usuário. Recarregue a página.", HttpStatus.CONFLICT);
        preencher(veiculo, request, true); veiculos.flush(); return resumo(veiculo);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Veiculo buscarParaAtualizacao(Long id) {
        return veiculos.buscarParaAtualizacao(id).orElseThrow(() -> ausente(id));
    }

    @Transactional(readOnly = true)
    public Veiculo buscarPorId(Long id) { return veiculos.findById(id).orElseThrow(() -> ausente(id)); }

    private void preencher(Veiculo v, VeiculoRequest r, boolean atualizacao) {
        v.setNome(obrigatorio(r.getNome(), "Nome").toUpperCase(Locale.ROOT));
        String placa = texto(r.getPlaca()); v.setPlaca(placa == null ? null : placa.toUpperCase(Locale.ROOT));
        v.setTipo(obrigatorio(r.getTipo(), "Tipo")); v.setMarcaFipe(texto(r.getMarcaFipe()));
        v.setModeloFipe(texto(r.getModeloFipe())); v.setAnoModelo(r.getAnoModelo()); v.setTipoMedidor(r.getTipoMedidor());
        v.setKmAtual(decimal(r.getKmAtual())); v.setHorimetroAtual(decimal(r.getHorimetroAtual()));
        if (r.getFipeReferenciaId() != null) {
            FipeDetalheResponse detalhe = fipe.buscarDetalhes(r.getFipeReferenciaId());
            if (detalhe == null) throw erro("FIPE_INDISPONIVEL", "Não foi possível confirmar a referência FIPE informada.");
            v.setValorFipe(detalhe.valor()); v.setUltimaConsultaFipe(detalhe.historico().toString());
        } else if (!atualizacao) {
            v.setValorFipe(decimal(r.getValorFipe())); v.setUltimaConsultaFipe(texto(r.getUltimaConsultaFipe()));
        }
        v.setAtivoPatrimonial(ativo(r.getAtivoPatrimonialId(), v.getId()));
        if (!StringUtils.hasText(v.getSituacao())) v.setSituacao("DISPONIVEL");
        v.setIcone(iconePorTipo(v.getTipo()));
    }

    private AtivoPatrimonial ativo(Long id, Long veiculoId) {
        if (id == null) return null;
        AtivoPatrimonial ativo = ativos.findById(id)
                .orElseThrow(() -> erro("ATIVO_NAO_ENCONTRADO", "Ativo patrimonial não encontrado.", HttpStatus.NOT_FOUND));
        if (!TIPOS_VINCULAVEIS.contains(ativo.getTipo()) || ativo.getStatus() == StatusAtivoPatrimonial.BAIXADO)
            throw erro("ATIVO_INCOMPATIVEL", "O ativo patrimonial não é compatível com a frota.");
        if (vinculadoAOutro(id, veiculoId))
            throw erro("ATIVO_JA_VINCULADO", "O ativo patrimonial já está vinculado a outro veículo.", HttpStatus.CONFLICT);
        return ativo;
    }

    private boolean vinculadoAOutro(Long ativoId, Long veiculoId) {
        return veiculoId == null ? veiculos.existsByAtivoPatrimonialId(ativoId)
                : veiculos.existsByAtivoPatrimonialIdAndIdNot(ativoId, veiculoId);
    }

    private void validar(VeiculoRequest r, boolean atualizacao) {
        if (r.getTipoMedidor() == null) throw erro("TIPO_MEDIDOR_OBRIGATORIO", "Informe o tipo de medidor.");
        if (r.getKmAtual() != null && r.getKmAtual().signum() < 0) throw erro("QUILOMETRAGEM_INVALIDA", "A quilometragem não pode ser negativa.");
        if (r.getHorimetroAtual() != null && r.getHorimetroAtual().signum() < 0) throw erro("HORIMETRO_INVALIDO", "O horímetro não pode ser negativo.");
        if (r.getValorFipe() != null && r.getValorFipe().signum() < 0) throw erro("VALOR_FIPE_INVALIDO", "O valor FIPE não pode ser negativo.");
        if (atualizacao && r.getVersao() == null) throw erro("VERSAO_OBRIGATORIA", "Informe a versão do veículo.");
    }

    private VeiculoResumo resumo(Veiculo v) {
        AtivoPatrimonial a = v.getAtivoPatrimonial();
        return new VeiculoResumo(v.getId(), v.getNome(), v.getPlaca(), v.getTipo(), v.getMarcaFipe(), v.getModeloFipe(),
                v.getAnoModelo(), v.getTipoMedidor(), v.getKmAtual(), v.getHorimetroAtual(), v.getSituacao(),
                v.getValorFipe(), v.getUltimaConsultaFipe(), v.getIcone(), a == null ? null : a.getId(),
                a == null ? null : a.getCodigo(), a == null ? null : a.getNome(), v.getVersao());
    }

    private String iconePorTipo(String tipo) { if ("2".equals(tipo)) return "fa-motorcycle"; if ("3".equals(tipo)) return "fa-truck"; return "fa-car"; }
    private String obrigatorio(String v, String campo) { if (!StringUtils.hasText(v)) throw erro("CAMPO_OBRIGATORIO", campo + " é obrigatório."); return v.trim(); }
    private String texto(String v) { return StringUtils.hasText(v) ? v.trim() : null; }
    private BigDecimal decimal(BigDecimal v) { return v == null ? null : v.setScale(2, RoundingMode.HALF_UP); }
    private FrotaOperacaoException ausente(Long id) { return erro("VEICULO_NAO_ENCONTRADO", "Veículo não encontrado: " + id, HttpStatus.NOT_FOUND); }
    private FrotaOperacaoException erro(String c, String m) { return new FrotaOperacaoException(c, m); }
    private FrotaOperacaoException erro(String c, String m, HttpStatus s) { return new FrotaOperacaoException(c, m, s); }
}
