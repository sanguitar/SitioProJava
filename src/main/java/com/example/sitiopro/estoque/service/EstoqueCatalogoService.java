package com.example.sitiopro.estoque.service;

import com.example.sitiopro.estoque.dto.CategoriaEstoqueRequest;
import com.example.sitiopro.estoque.dto.ItemEstoqueRequest;
import com.example.sitiopro.estoque.dto.LocalEstoqueRequest;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.UnidadeMedida;
import com.example.sitiopro.estoque.repository.CategoriaEstoqueRepository;
import com.example.sitiopro.estoque.repository.ItemEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.UnidadeMedidaRepository;
import com.example.sitiopro.shared.observability.MdcScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class EstoqueCatalogoService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueCatalogoService.class);

    private final CategoriaEstoqueRepository categoriaRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final LocalEstoqueRepository localRepository;
    private final ItemEstoqueRepository itemRepository;

    public EstoqueCatalogoService(CategoriaEstoqueRepository categoriaRepository,
            UnidadeMedidaRepository unidadeMedidaRepository,
            LocalEstoqueRepository localRepository,
            ItemEstoqueRepository itemRepository) {
        this.categoriaRepository = categoriaRepository;
        this.unidadeMedidaRepository = unidadeMedidaRepository;
        this.localRepository = localRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaEstoque> listarCategorias() {
        return categoriaRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<CategoriaEstoque> listarCategoriasAtivas() {
        return categoriaRepository.findByAtivaTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<UnidadeMedida> listarUnidadesAtivas() {
        return unidadeMedidaRepository.findByAtivaTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<LocalEstoque> listarLocais() {
        return localRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<LocalEstoque> listarLocaisAtivos() {
        return localRepository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<ItemEstoque> listarItens() {
        return itemRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<ItemEstoque> listarItensAtivos() {
        return itemRepository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public ItemEstoque buscarItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EstoqueOperacaoException("ITEM_NAO_ENCONTRADO",
                        "Item de estoque não encontrado.", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public LocalEstoque buscarLocalAtivo(Long id) {
        if (id == null) {
            throw new EstoqueOperacaoException("LOCAL_DESTINO_OBRIGATORIO", "Informe o local de destino.");
        }
        return localRepository.findById(id)
                .filter(LocalEstoque::isAtivo)
                .orElseThrow(() -> new EstoqueOperacaoException("LOCAL_ESTOQUE_INATIVO",
                        "Local de estoque não encontrado ou inativo."));
    }

    @Transactional
    public CategoriaEstoque criarCategoria(CategoriaEstoqueRequest request) {
        String nome = normalizarNome(request.getNome());
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new EstoqueOperacaoException("CATEGORIA_DUPLICADA", "Já existe categoria de estoque com esse nome.");
        }
        CategoriaEstoque categoria = new CategoriaEstoque();
        categoria.setNome(nome);
        categoria.setDescricao(normalizarTextoOpcional(request.getDescricao()));
        categoria.setAtiva(request.isAtiva());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public LocalEstoque criarLocal(LocalEstoqueRequest request) {
        String nome = normalizarNome(request.getNome());
        if (localRepository.existsByNomeIgnoreCase(nome)) {
            throw new EstoqueOperacaoException("LOCAL_DUPLICADO", "Já existe local de estoque com esse nome.");
        }
        LocalEstoque local = new LocalEstoque();
        local.setNome(nome);
        local.setDescricao(normalizarTextoOpcional(request.getDescricao()));
        local.setAtivo(request.isAtivo());
        return localRepository.save(local);
    }

    @Transactional
    public ItemEstoque criarItem(ItemEstoqueRequest request) {
        String nome = normalizarNome(request.getNome());
        validarItem(request, nome);

        CategoriaEstoque categoria = categoriaRepository.findById(request.getCategoriaId())
                .filter(CategoriaEstoque::isAtiva)
                .orElseThrow(() -> new EstoqueOperacaoException("CATEGORIA_INVALIDA",
                        "Categoria de estoque não encontrada ou inativa."));
        UnidadeMedida unidade = unidadeMedidaRepository.findById(request.getUnidadeMedidaId())
                .filter(UnidadeMedida::isAtiva)
                .orElseThrow(() -> new EstoqueOperacaoException("UNIDADE_INVALIDA",
                        "Unidade de medida não encontrada ou inativa."));

        ItemEstoque item = new ItemEstoque();
        item.setNome(nome);
        item.setDescricao(normalizarTextoOpcional(request.getDescricao()));
        item.setCategoria(categoria);
        item.setUnidadeMedida(unidade);
        item.setEstoqueMinimo(escala(request.getEstoqueMinimo()));
        item.setAtivo(request.isAtivo());
        item.setControlaLote(request.isControlaLote() || request.isControlaValidade());
        item.setControlaValidade(request.isControlaValidade());
        ItemEstoque salvo = itemRepository.save(item);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "estoque.item.criado",
                "module", "estoque",
                "estoque.item.id", salvo.getId() == null ? "unknown" : String.valueOf(salvo.getId())))) {
            log.info("Item de estoque criado.");
        }
        return salvo;
    }

    private void validarItem(ItemEstoqueRequest request, String nome) {
        if (itemRepository.existsByNomeIgnoreCase(nome)) {
            throw new EstoqueOperacaoException("ITEM_DUPLICADO", "Já existe item de estoque com esse nome.");
        }
        if (request.isControlaValidade() && !request.isControlaLote()) {
            throw new EstoqueOperacaoException("VALIDADE_REQUER_LOTE",
                    "Itens com controle de validade também precisam controlar lote.");
        }
        BigDecimal minimo = request.getEstoqueMinimo();
        if (minimo != null && minimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new EstoqueOperacaoException("ESTOQUE_MINIMO_INVALIDO",
                    "Estoque mínimo não pode ser negativo.");
        }
    }

    private String normalizarNome(String valor) {
        if (!StringUtils.hasText(valor)) {
            throw new EstoqueOperacaoException("NOME_OBRIGATORIO", "Informe um nome.");
        }
        return valor.trim();
    }

    private String normalizarTextoOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor == null ? null : valor.stripTrailingZeros();
    }
}
