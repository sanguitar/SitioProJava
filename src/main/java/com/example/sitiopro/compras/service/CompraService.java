package com.example.sitiopro.compras.service;

import com.example.sitiopro.compras.dto.CompraDetalhe;
import com.example.sitiopro.compras.dto.CompraFiltro;
import com.example.sitiopro.compras.dto.CompraRequest;
import com.example.sitiopro.compras.dto.CompraResumo;
import com.example.sitiopro.compras.dto.ComprasDashboardResumo;
import com.example.sitiopro.compras.dto.FornecedorResumo;
import com.example.sitiopro.compras.dto.ItemCompraRequest;
import com.example.sitiopro.compras.dto.ItemCompraResumo;
import com.example.sitiopro.compras.entity.Compra;
import com.example.sitiopro.compras.entity.Fornecedor;
import com.example.sitiopro.compras.entity.ItemCompra;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.repository.CompraRepository;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import com.example.sitiopro.shared.observability.MdcScope;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CompraService {

    private static final Logger log = LoggerFactory.getLogger(CompraService.class);
    private static final int ESCALA = 4;

    private final CompraRepository compraRepository;
    private final FornecedorService fornecedorService;
    private final EstoqueCatalogoService estoqueCatalogoService;
    private final EstoqueMovimentoService estoqueMovimentoService;

    public CompraService(CompraRepository compraRepository,
            FornecedorService fornecedorService,
            EstoqueCatalogoService estoqueCatalogoService,
            EstoqueMovimentoService estoqueMovimentoService) {
        this.compraRepository = compraRepository;
        this.fornecedorService = fornecedorService;
        this.estoqueCatalogoService = estoqueCatalogoService;
        this.estoqueMovimentoService = estoqueMovimentoService;
    }

    @Transactional(readOnly = true)
    public List<CompraResumo> listar(CompraFiltro filtro) {
        CompraFiltro filtroSeguro = filtro == null ? new CompraFiltro() : filtro;
        return compraRepository.buscar(filtroSeguro.getStatus(), filtroSeguro.getFornecedorId(),
                        filtroSeguro.getInicio(), filtroSeguro.getFim())
                .stream()
                .map(this::paraResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComprasDashboardResumo montarResumo() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        return new ComprasDashboardResumo(
                compraRepository.contarConfirmadasEntre(inicioMes, hoje),
                escala(compraRepository.somarTotalConfirmadoEntre(inicioMes, hoje)),
                compraRepository.countByStatus(StatusCompra.RASCUNHO),
                fornecedorService.contarAtivos(),
                compraRepository.findFirstByStatusOrderByDataCompraDescIdDesc(StatusCompra.CONFIRMADA)
                        .map(this::paraResumo)
                        .orElse(null),
                compraRepository.findTop10ByOrderByDataCompraDescIdDesc().stream()
                        .map(this::paraResumo)
                        .toList());
    }

    @Transactional(readOnly = true)
    public CompraDetalhe detalhar(Long id) {
        return paraDetalhe(buscarCompra(id));
    }

    @Transactional
    public CompraDetalhe criarCompra(CompraRequest request) {
        Fornecedor fornecedor = fornecedorService.buscarAtivoPorId(request.getFornecedorId());
        validarFornecedorAtivo(fornecedor);

        Compra compra = new Compra();
        aplicarCabecalho(compra, fornecedor, request);
        compra.setStatus(StatusCompra.RASCUNHO);
        recalcularTotais(compra);

        Compra salva = compraRepository.save(compra);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "compra_criada",
                "module", "compras",
                "compras.compra.id", safeId(salva.getId()),
                "compras.fornecedor.id", safeId(fornecedor.getId())))) {
            log.info("Compra criada como rascunho.");
        }
        return paraDetalhe(salva);
    }

    @Transactional
    public CompraDetalhe atualizarRascunho(Long id, CompraRequest request) {
        Compra compra = buscarCompraRascunho(id);
        Fornecedor fornecedor = fornecedorService.buscarAtivoPorId(request.getFornecedorId());
        validarFornecedorAtivo(fornecedor);
        aplicarCabecalho(compra, fornecedor, request);
        recalcularTotais(compra);
        return paraDetalhe(compraRepository.save(compra));
    }

    @Transactional
    public CompraDetalhe adicionarItem(Long compraId, ItemCompraRequest request) {
        Compra compra = buscarCompraRascunho(compraId);
        ItemEstoque itemEstoque = estoqueCatalogoService.buscarItem(request.getItemEstoqueId());
        validarItemAtivo(itemEstoque);
        validarLoteCompra(itemEstoque, request);
        LocalEstoque localDestino = estoqueCatalogoService.buscarLocalAtivo(request.getLocalDestinoId());

        ItemCompra item = new ItemCompra();
        item.setItemEstoque(itemEstoque);
        item.setLocalDestino(localDestino);
        item.setQuantidade(quantidadePositiva(request.getQuantidade()));
        item.setCustoUnitario(valorNaoNegativo(request.getCustoUnitario(), "CUSTO_INVALIDO",
                "Custo unitário não pode ser negativo."));
        item.setLoteCodigo(normalizarTextoOpcional(request.getLoteCodigo()));
        item.setValidade(request.getValidade());
        item.setSubtotal(calcularSubtotalItem(item));
        compra.adicionarItem(item);
        recalcularTotais(compra);

        Compra salva = compraRepository.save(compra);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "compra_item_adicionado",
                "module", "compras",
                "compras.compra.id", safeId(compra.getId()),
                "estoque.item.id", safeId(itemEstoque.getId())))) {
            log.info("Item adicionado à compra.");
        }
        return paraDetalhe(salva);
    }

    @Transactional
    public CompraDetalhe removerItem(Long compraId, Long itemId) {
        Compra compra = buscarCompraRascunho(compraId);
        ItemCompra item = compra.getItens().stream()
                .filter(candidato -> Objects.equals(candidato.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ComprasOperacaoException("ITEM_COMPRA_NAO_ENCONTRADO",
                        "Item da compra não encontrado.", HttpStatus.NOT_FOUND));
        compra.removerItem(item);
        recalcularTotais(compra);
        return paraDetalhe(compraRepository.save(compra));
    }

    @Transactional
    public CompraDetalhe atualizarItem(Long compraId, Long itemId, ItemCompraRequest request) {
        Compra compra = buscarCompraRascunho(compraId);
        ItemCompra item = compra.getItens().stream()
                .filter(candidato -> Objects.equals(candidato.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new ComprasOperacaoException("ITEM_COMPRA_NAO_ENCONTRADO",
                        "Item da compra não encontrado.", HttpStatus.NOT_FOUND));

        ItemEstoque itemEstoque = estoqueCatalogoService.buscarItem(request.getItemEstoqueId());
        validarItemAtivo(itemEstoque);
        validarLoteCompra(itemEstoque, request);
        LocalEstoque localDestino = estoqueCatalogoService.buscarLocalAtivo(request.getLocalDestinoId());

        item.setItemEstoque(itemEstoque);
        item.setLocalDestino(localDestino);
        item.setQuantidade(quantidadePositiva(request.getQuantidade()));
        item.setCustoUnitario(valorNaoNegativo(request.getCustoUnitario(), "CUSTO_INVALIDO",
                "Custo unitário não pode ser negativo."));
        item.setLoteCodigo(normalizarTextoOpcional(request.getLoteCodigo()));
        item.setValidade(request.getValidade());
        item.setSubtotal(calcularSubtotalItem(item));
        recalcularTotais(compra);

        Compra salva = compraRepository.save(compra);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "compra_item_atualizado",
                "module", "compras",
                "compras.compra.id", safeId(compra.getId()),
                "compras.item.id", safeId(item.getId())))) {
            log.info("Item da compra atualizado.");
        }
        return paraDetalhe(salva);
    }

    @Transactional
    public CompraDetalhe confirmarCompra(Long id) {
        try {
            Compra compra = compraRepository.buscarParaConfirmacao(id)
                    .orElseThrow(() -> new ComprasOperacaoException("COMPRA_NAO_ENCONTRADA",
                            "Compra não encontrada.", HttpStatus.NOT_FOUND));
            validarConfirmacao(compra);
            recalcularTotais(compra);

            for (ItemCompra item : compra.getItens().stream()
                    .sorted(Comparator.comparing(ItemCompra::getId, Comparator.nullsLast(Long::compareTo)))
                    .toList()) {
                MovimentoEstoque movimento = estoqueMovimentoService.registrarEntradaCompra(movimentoRequest(compra, item),
                        compra.getId());
                item.setMovimentoEstoque(movimento);
            }

            compra.setStatus(StatusCompra.CONFIRMADA);
            compra.setConfirmadoEm(LocalDateTime.now());
            compra.setConfirmadoPor(usuarioAtual());
            Compra salva = compraRepository.save(compra);
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", "compra_confirmada",
                    "module", "compras",
                    "compras.compra.id", safeId(salva.getId()),
                    "compras.itens.quantidade", String.valueOf(salva.getItens().size())))) {
                log.info("Compra confirmada e entradas de estoque registradas.");
            }
            return paraDetalhe(salva);
        } catch (OptimisticLockingFailureException | OptimisticLockException ex) {
            throw new ComprasOperacaoException("COMPRA_CONCORRENTE",
                    "A compra foi alterada por outro processo. Recarregue a tela e tente novamente.",
                    HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public CompraDetalhe cancelarRascunho(Long id) {
        Compra compra = buscarCompraRascunho(id);
        compra.setStatus(StatusCompra.CANCELADA);
        Compra salva = compraRepository.save(compra);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "compra_cancelada",
                "module", "compras",
                "compras.compra.id", safeId(salva.getId())))) {
            log.info("Compra cancelada.");
        }
        return paraDetalhe(salva);
    }

    private Compra buscarCompra(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new ComprasOperacaoException("COMPRA_NAO_ENCONTRADA",
                        "Compra não encontrada.", HttpStatus.NOT_FOUND));
    }

    private Compra buscarCompraRascunho(Long id) {
        Compra compra = buscarCompra(id);
        if (compra.getStatus() == StatusCompra.CONFIRMADA) {
            throw new ComprasOperacaoException("COMPRA_JA_CONFIRMADA",
                    "Compra confirmada não pode ser alterada.", HttpStatus.CONFLICT);
        }
        if (compra.getStatus() == StatusCompra.CANCELADA) {
            throw new ComprasOperacaoException("COMPRA_CANCELADA",
                    "Compra cancelada não pode ser alterada.", HttpStatus.CONFLICT);
        }
        return compra;
    }

    private void aplicarCabecalho(Compra compra, Fornecedor fornecedor, CompraRequest request) {
        compra.setFornecedor(fornecedor);
        compra.setDataCompra(request.getDataCompra() == null ? LocalDate.now() : request.getDataCompra());
        compra.setNumeroDocumento(normalizarTextoOpcional(request.getNumeroDocumento()));
        compra.setObservacao(normalizarTextoOpcional(request.getObservacao()));
        compra.setFrete(valorNaoNegativo(request.getFrete(), "FRETE_INVALIDO", "Frete não pode ser negativo."));
        compra.setDesconto(valorNaoNegativo(request.getDesconto(), "DESCONTO_INVALIDO",
                "Desconto não pode ser negativo."));
    }

    private void validarConfirmacao(Compra compra) {
        if (compra.getStatus() == StatusCompra.CONFIRMADA) {
            throw new ComprasOperacaoException("COMPRA_JA_CONFIRMADA",
                    "Compra já confirmada. Nenhuma entrada de estoque foi duplicada.", HttpStatus.CONFLICT);
        }
        if (compra.getStatus() == StatusCompra.CANCELADA) {
            throw new ComprasOperacaoException("COMPRA_CANCELADA",
                    "Compra cancelada não pode ser confirmada.", HttpStatus.CONFLICT);
        }
        if (compra.getItens().isEmpty()) {
            throw new ComprasOperacaoException("COMPRA_SEM_ITENS",
                    "Inclua ao menos um item antes de confirmar a compra.");
        }
        validarFornecedorAtivo(compra.getFornecedor());
        for (ItemCompra item : compra.getItens()) {
            if (item.getMovimentoEstoque() != null) {
                throw new ComprasOperacaoException("COMPRA_CONCORRENTE",
                        "Esta compra já possui movimentação vinculada. Recarregue a tela.",
                        HttpStatus.CONFLICT);
            }
            validarItemAtivo(item.getItemEstoque());
            if (!item.getLocalDestino().isAtivo()) {
                throw new ComprasOperacaoException("LOCAL_ESTOQUE_INATIVO",
                        "Local de destino não encontrado ou inativo.");
            }
            validarLoteCompra(item.getItemEstoque(), item.getLoteCodigo(), item.getValidade());
        }
    }

    private void validarFornecedorAtivo(Fornecedor fornecedor) {
        if (!fornecedor.isAtivo()) {
            throw new ComprasOperacaoException("FORNECEDOR_INATIVO",
                    "Fornecedor inativo não pode ser usado em compras.");
        }
    }

    private void validarItemAtivo(ItemEstoque itemEstoque) {
        if (!itemEstoque.isAtivo()) {
            throw new ComprasOperacaoException("ITEM_ESTOQUE_INATIVO",
                    "Item de estoque inativo não pode ser comprado.");
        }
    }

    private void validarLoteCompra(ItemEstoque itemEstoque, ItemCompraRequest request) {
        validarLoteCompra(itemEstoque, request.getLoteCodigo(), request.getValidade());
    }

    private void validarLoteCompra(ItemEstoque itemEstoque, String loteCodigo, LocalDate validade) {
        boolean informouLote = StringUtils.hasText(loteCodigo);
        if (!itemEstoque.isControlaLote()) {
            if (informouLote || validade != null) {
                throw new ComprasOperacaoException("ITEM_NAO_CONTROLA_LOTE",
                        "Este item não controla lote ou validade.");
            }
            return;
        }
        if (!informouLote) {
            throw new ComprasOperacaoException("LOTE_OBRIGATORIO",
                    "Informe o lote para este item.");
        }
        if (itemEstoque.isControlaValidade() && validade == null) {
            throw new ComprasOperacaoException("VALIDADE_OBRIGATORIA",
                    "Informe a validade para este item.");
        }
    }

    private MovimentoEstoqueRequest movimentoRequest(Compra compra, ItemCompra item) {
        MovimentoEstoqueRequest request = new MovimentoEstoqueRequest();
        request.setItemId(item.getItemEstoque().getId());
        request.setQuantidade(item.getQuantidade());
        request.setLocalDestinoId(item.getLocalDestino().getId());
        request.setLoteCodigo(item.getLoteCodigo());
        request.setValidade(item.getValidade());
        request.setCustoUnitario(item.getCustoUnitario());
        request.setCustoTotal(item.getSubtotal());
        request.setObservacao("Compra #" + compra.getId() + " - " + compra.getFornecedor().getNome());
        request.setDataMovimento(LocalDateTime.now());
        return request;
    }

    private void recalcularTotais(Compra compra) {
        BigDecimal subtotal = compra.getItens().stream()
                .peek(item -> item.setSubtotal(calcularSubtotalItem(item)))
                .map(ItemCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subtotal.add(compra.getFrete()).subtract(compra.getDesconto());
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            throw new ComprasOperacaoException("TOTAL_INVALIDO",
                    "Total da compra não pode ficar negativo.");
        }
        compra.setSubtotal(escala(subtotal));
        compra.setTotal(escala(total));
    }

    private BigDecimal calcularSubtotalItem(ItemCompra item) {
        return escala(item.getQuantidade().multiply(item.getCustoUnitario()));
    }

    private BigDecimal quantidadePositiva(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ComprasOperacaoException("QUANTIDADE_INVALIDA", "Quantidade deve ser maior que zero.");
        }
        return escala(valor);
    }

    private BigDecimal valorNaoNegativo(BigDecimal valor, String code, String message) {
        BigDecimal seguro = valor == null ? BigDecimal.ZERO : valor;
        if (seguro.compareTo(BigDecimal.ZERO) < 0) {
            throw new ComprasOperacaoException(code, message);
        }
        return escala(seguro);
    }

    private CompraDetalhe paraDetalhe(Compra compra) {
        return new CompraDetalhe(
                compra.getId(),
                fornecedorService.paraResumo(compra.getFornecedor()),
                compra.getDataCompra(),
                compra.getNumeroDocumento(),
                compra.getObservacao(),
                compra.getStatus(),
                compra.getStatus().getRotulo(),
                compra.getSubtotal(),
                compra.getFrete(),
                compra.getDesconto(),
                compra.getTotal(),
                compra.getItens().stream()
                        .sorted(Comparator.comparing(ItemCompra::getId, Comparator.nullsLast(Long::compareTo)))
                        .map(this::paraResumoItem)
                        .toList(),
                compra.getConfirmadoEm(),
                compra.getConfirmadoPor(),
                compra.getCriadoEm(),
                compra.getCriadoPor(),
                compra.getAlteradoEm(),
                compra.getAlteradoPor());
    }

    private CompraResumo paraResumo(Compra compra) {
        return new CompraResumo(
                compra.getId(),
                compra.getFornecedor().getId(),
                compra.getFornecedor().getNome(),
                compra.getDataCompra(),
                compra.getNumeroDocumento(),
                compra.getStatus(),
                compra.getStatus().getRotulo(),
                compra.getSubtotal(),
                compra.getFrete(),
                compra.getDesconto(),
                compra.getTotal(),
                compra.getItens().size(),
                compra.getConfirmadoEm(),
                compra.getConfirmadoPor());
    }

    private ItemCompraResumo paraResumoItem(ItemCompra item) {
        MovimentoEstoque movimento = item.getMovimentoEstoque();
        return new ItemCompraResumo(
                item.getId(),
                item.getItemEstoque().getId(),
                item.getItemEstoque().getNome(),
                item.getItemEstoque().getUnidadeMedida().getSigla(),
                item.getQuantidade(),
                item.getCustoUnitario(),
                item.getSubtotal(),
                item.getLocalDestino().getId(),
                item.getLocalDestino().getNome(),
                item.getLoteCodigo(),
                item.getValidade(),
                movimento == null ? null : movimento.getId());
    }

    private String usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioPrincipal usuario) {
            return usuario.getUsername();
        }
        String name = authentication.getName();
        return StringUtils.hasText(name) ? name : "system";
    }

    private String normalizarTextoOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor.setScale(ESCALA, RoundingMode.HALF_UP);
    }

    private String safeId(Long id) {
        return id == null ? "unknown" : String.valueOf(id);
    }
}
