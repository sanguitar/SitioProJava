package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import com.example.sitiopro.criacao.aves.repository.LoteAvesRepository;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoRequest;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoResumo;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import com.example.sitiopro.criacao.core.repository.InstalacaoCriacaoRepository;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.propriedade.service.PropriedadeOperacaoException;
import com.example.sitiopro.propriedade.dto.CadastroFisicoResumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class InstalacaoCriacaoService {
    private static final Logger log = LoggerFactory.getLogger(InstalacaoCriacaoService.class);
    private final InstalacaoCriacaoRepository repository;
    private final LoteAvesRepository loteRepository;
    private final PropriedadeService propriedadeService;

    public InstalacaoCriacaoService(InstalacaoCriacaoRepository repository, LoteAvesRepository loteRepository,
            PropriedadeService propriedadeService) {
        this.repository = repository;
        this.loteRepository = loteRepository;
        this.propriedadeService = propriedadeService;
    }

    @Transactional(readOnly = true)
    public List<CadastroFisicoResumo> estruturasDisponiveis() { return propriedadeService.estruturasDisponiveis(); }

    @Transactional(readOnly = true)
    public PaginaResponse<InstalacaoCriacaoResumo> listar(int pagina, int tamanho) {
        return PaginaResponse.de(repository.findAllByOrderByAtivoDescNomeAsc(
                PageRequest.of(normalizarPagina(pagina), normalizarTamanho(tamanho))).map(this::resumo));
    }

    @Transactional(readOnly = true)
    public List<InstalacaoCriacaoResumo> listarAtivas() {
        return repository.findByAtivoTrueOrderByNomeAsc().stream().map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public List<InstalacaoCriacaoResumo> listarIncubadorasAtivas() {
        return repository.findByAtivoTrueAndTipoOrderByNomeAsc(TipoInstalacaoCriacao.INCUBADORA).stream()
                .map(this::resumo).toList();
    }

    @Transactional(readOnly = true)
    public InstalacaoCriacaoResumo detalhar(Long id) { return resumo(buscar(id)); }

    @Transactional(readOnly = true)
    public InstalacaoCriacaoRequest formulario(Long id) {
        InstalacaoCriacao atual = buscar(id);
        InstalacaoCriacaoRequest request = new InstalacaoCriacaoRequest();
        request.setNome(atual.getNome());
        request.setTipo(atual.getTipo());
        request.setDescricao(atual.getDescricao());
        request.setCapacidade(atual.getCapacidade());
        request.setAtivo(atual.isAtivo());
        request.setEstruturaId(atual.getEstrutura() == null ? null : atual.getEstrutura().getId());
        return request;
    }

    @Transactional
    public InstalacaoCriacaoResumo criar(InstalacaoCriacaoRequest request) {
        String nome = nome(request.getNome());
        if (repository.existsByNomeIgnoreCase(nome)) throw conflito("INSTALACAO_DUPLICADA", "Já existe instalação com esse nome.");
        validar(request);
        InstalacaoCriacao instalacao = new InstalacaoCriacao();
        aplicar(instalacao, request, nome);
        InstalacaoCriacao salvo = repository.save(instalacao);
        log(salvo, "criacao.aves.installation.created");
        return resumo(salvo);
    }

    @Transactional
    public InstalacaoCriacaoResumo atualizar(Long id, InstalacaoCriacaoRequest request) {
        InstalacaoCriacao instalacao = repository.buscarParaAtualizacao(id).orElseThrow(() -> naoEncontrada(id));
        String nome = nome(request.getNome());
        if (repository.existsByNomeIgnoreCaseAndIdNot(nome, id)) throw conflito("INSTALACAO_DUPLICADA", "Já existe instalação com esse nome.");
        validar(request);
        long ocupacao = ocupacao(id, null);
        if (!request.isAtivo() && ocupacao > 0) throw conflito("INSTALACAO_OCUPADA", "Transfira os lotes ativos antes de inativar a instalação.");
        if (request.getCapacidade() != null && ocupacao > request.getCapacidade()) throw conflito("CAPACIDADE_INFERIOR_OCUPACAO", "A capacidade não pode ser menor que a ocupação atual.");
        aplicar(instalacao, request, nome);
        log(instalacao, "criacao.aves.installation.updated");
        return resumo(instalacao);
    }

    @Transactional(readOnly = true)
    public InstalacaoCriacao buscarAtiva(Long id) {
        return repository.findById(id).filter(InstalacaoCriacao::isAtivo)
                .orElseThrow(() -> new AvesOperacaoException("INSTALACAO_INVALIDA", "Instalação não encontrada ou inativa."));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public InstalacaoCriacao reservarCapacidade(Long id, int quantidade, Long loteIgnorado) {
        InstalacaoCriacao instalacao = repository.buscarParaAtualizacao(id)
                .filter(InstalacaoCriacao::isAtivo)
                .orElseThrow(() -> new AvesOperacaoException(
                        "INSTALACAO_INVALIDA", "Instalação não encontrada ou inativa."));
        validarCapacidade(instalacao, quantidade, loteIgnorado);
        return instalacao;
    }

    public void validarCapacidade(InstalacaoCriacao instalacao, int quantidade, Long loteIgnorado) {
        if (instalacao.getCapacidade() == null) return;
        long projetada = ocupacao(instalacao.getId(), loteIgnorado) + quantidade;
        if (projetada > instalacao.getCapacidade()) {
            throw new AvesOperacaoException("CAPACIDADE_EXCEDIDA", "A instalação não possui capacidade para esse lote.", HttpStatus.CONFLICT);
        }
    }

    private InstalacaoCriacao buscar(Long id) { return repository.findById(id).orElseThrow(() -> naoEncontrada(id)); }
    private AvesOperacaoException naoEncontrada(Long id) { return new AvesOperacaoException("INSTALACAO_NAO_ENCONTRADA", "Instalação não encontrada: " + id, HttpStatus.NOT_FOUND); }
    private long ocupacao(Long id, Long ignorar) { return loteRepository.somarOcupacao(id, StatusLoteAves.ATIVO, ignorar); }
    private void validar(InstalacaoCriacaoRequest r) { if (r.getTipo() == null) throw new AvesOperacaoException("TIPO_OBRIGATORIO", "Informe o tipo da instalação."); if (r.getCapacidade() != null && r.getCapacidade() < 1) throw new AvesOperacaoException("CAPACIDADE_INVALIDA", "Capacidade deve ser maior que zero."); }
    private void aplicar(InstalacaoCriacao i, InstalacaoCriacaoRequest r, String nome) {
        try {
            i.setEstrutura(r.getEstruturaId() == null ? null : propriedadeService.estruturaParaVinculo(r.getEstruturaId()));
        } catch (PropriedadeOperacaoException ex) {
            throw new AvesOperacaoException("ESTRUTURA_INVALIDA", ex.getMessage(), ex.getStatus());
        }
        i.setNome(nome); i.setTipo(r.getTipo()); i.setDescricao(texto(r.getDescricao()));
        i.setCapacidade(r.getCapacidade()); i.setAtivo(r.isAtivo());
    }
    private InstalacaoCriacaoResumo resumo(InstalacaoCriacao i) { return new InstalacaoCriacaoResumo(i.getId(), i.getNome(), i.getTipo(), i.getTipo().getRotulo(), i.getDescricao(), i.getCapacidade(), ocupacao(i.getId(), null), i.isAtivo(), i.getVersao(), i.getCriadoEm(), i.getCriadoPor(), i.getAlteradoEm(), i.getAlteradoPor(),
            i.getEstrutura() == null ? null : i.getEstrutura().getId(),
            i.getEstrutura() == null ? null : i.getEstrutura().getNome()); }
    private String nome(String valor) { if (!StringUtils.hasText(valor)) throw new AvesOperacaoException("NOME_OBRIGATORIO", "Informe o nome da instalação."); return valor.trim(); }
    private String texto(String valor) { return StringUtils.hasText(valor) ? valor.trim() : null; }
    private int normalizarPagina(int p) { return Math.max(0, p); }
    private int normalizarTamanho(int t) { return Math.min(100, Math.max(1, t)); }
    private AvesOperacaoException conflito(String c, String m) { return new AvesOperacaoException(c, m, HttpStatus.CONFLICT); }
    private void log(InstalacaoCriacao i, String evento) { try (MdcScope ignored = MdcScope.with(Map.of("event.action", evento, "module", "criacoes", "criacao.instalacao.id", String.valueOf(i.getId())))) { log.info("Instalação de criação atualizada."); } }
}
