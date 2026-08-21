package com.example.sitiopro.estoque.service;

import com.example.sitiopro.estoque.dto.EstoqueDashboardResumo;
import com.example.sitiopro.estoque.dto.ItemEstoqueDetalhe;
import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.dto.LoteEstoqueResumo;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueResponse;
import com.example.sitiopro.estoque.dto.SaldoLocalEstoque;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.LoteEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import com.example.sitiopro.estoque.repository.ItemEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.LoteEstoqueRepository;
import com.example.sitiopro.estoque.repository.MovimentoEstoqueRepository;
import com.example.sitiopro.shared.observability.MdcScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class EstoqueMovimentoService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueMovimentoService.class);
    private static final int ESCALA = 4;

    private final ItemEstoqueRepository itemRepository;
    private final LocalEstoqueRepository localRepository;
    private final LoteEstoqueRepository loteRepository;
    private final MovimentoEstoqueRepository movimentoRepository;

    public EstoqueMovimentoService(ItemEstoqueRepository itemRepository,
            LocalEstoqueRepository localRepository,
            LoteEstoqueRepository loteRepository,
            MovimentoEstoqueRepository movimentoRepository) {
        this.itemRepository = itemRepository;
        this.localRepository = localRepository;
        this.loteRepository = loteRepository;
        this.movimentoRepository = movimentoRepository;
    }

    @Transactional
    public MovimentoEstoqueResponse registrarMovimento(MovimentoEstoqueRequest request,
            boolean ajusteAdministrativoPermitido) {
        return paraResponse(registrarMovimentoInterno(request, ajusteAdministrativoPermitido, null, null, null));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public MovimentoEstoque registrarEntradaCompra(MovimentoEstoqueRequest request, Long compraId) {
        if (compraId == null) {
            throw new EstoqueOperacaoException("ORIGEM_COMPRA_OBRIGATORIA",
                    "Informe a compra de origem para registrar entrada.");
        }
        MovimentoEstoqueRequest entrada = copiarComoEntrada(request);
        return registrarMovimentoInterno(entrada, false, "compras", compraId, "Compra #" + compraId);
    }

    private MovimentoEstoque registrarMovimentoInterno(MovimentoEstoqueRequest request,
            boolean ajusteAdministrativoPermitido,
            String origemModulo,
            Long origemReferenciaId,
            String origemDescricao) {
        validarQuantidade(request.getQuantidade());
        TipoMovimentoEstoque tipo = request.getTipo();
        if (tipo == null) {
            throw new EstoqueOperacaoException("TIPO_OBRIGATORIO", "Informe o tipo da movimentação.");
        }
        if (tipo.ajusteAdministrativo() && !ajusteAdministrativoPermitido) {
            throw new EstoqueOperacaoException("AJUSTE_RESTRITO",
                    "Ajustes administrativos de estoque exigem perfil ADMIN.", HttpStatus.FORBIDDEN);
        }

        ItemEstoque item = buscarItem(request.getItemId());
        if (!item.isAtivo()) {
            throw new EstoqueOperacaoException("ITEM_INATIVO",
                    "Item inativo não aceita movimentação de estoque.");
        }

        LocalEstoque origem = resolverOrigem(tipo, request);
        LocalEstoque destino = resolverDestino(tipo, request);
        LoteEstoque lote = resolverLote(item, tipo, request);
        validarCusto(tipo, request);

        if (tipo.reduzOrigem()) {
            validarSaldoDisponivel(item, origem, lote, request.getQuantidade());
        }

        MovimentoEstoque movimento = new MovimentoEstoque();
        movimento.setItem(item);
        movimento.setTipo(tipo);
        movimento.setQuantidade(normalizarDecimal(request.getQuantidade()));
        movimento.setLocalOrigem(origem);
        movimento.setLocalDestino(destino);
        movimento.setLote(lote);
        movimento.setObservacao(normalizarTextoOpcional(request.getObservacao()));
        movimento.setDataMovimento(request.getDataMovimento() == null
                ? LocalDateTime.now()
                : request.getDataMovimento());
        movimento.setOrigemModulo(origemModulo);
        movimento.setOrigemReferenciaId(origemReferenciaId);
        movimento.setOrigemDescricao(origemDescricao);
        aplicarCustos(tipo, request, movimento);

        MovimentoEstoque salvo = movimentoRepository.save(movimento);
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "estoque.movimento.registrado",
                "module", "estoque",
                "estoque.item.id", safeId(item.getId()),
                "estoque.movimento.id", safeId(salvo.getId()),
                "estoque.movimento.tipo", tipo.name()))) {
            log.info("Movimentação de estoque concluída.");
        }
        return salvo;
    }

    @Transactional(readOnly = true)
    public List<ItemEstoqueResumo> listarItensComSaldo() {
        return itemRepository.findAllByOrderByNomeAsc().stream()
                .map(this::paraResumoItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemEstoqueDetalhe detalharItem(Long id) {
        ItemEstoque item = buscarItem(id);
        List<SaldoLocalEstoque> saldos = localRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(local -> new SaldoLocalEstoque(local.getId(), local.getNome(), saldoItemLocal(item.getId(), local.getId())))
                .filter(saldo -> saldo.saldo().compareTo(BigDecimal.ZERO) != 0)
                .toList();
        List<MovimentoEstoqueResponse> movimentos = movimentoRepository.findByItemIdOrderByDataMovimentoDescIdDesc(id)
                .stream()
                .map(this::paraResponse)
                .toList();
        return new ItemEstoqueDetalhe(paraResumoItem(item), item.getDescricao(), item.isControlaLote(),
                item.isControlaValidade(), saldos, movimentos);
    }

    @Transactional(readOnly = true)
    public List<MovimentoEstoqueResponse> listarMovimentos() {
        return movimentoRepository.findAllByOrderByDataMovimentoDescIdDesc().stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimentoEstoqueResponse buscarMovimento(Long id) {
        return movimentoRepository.findById(id)
                .map(this::paraResponse)
                .orElseThrow(() -> new EstoqueOperacaoException("MOVIMENTO_NAO_ENCONTRADO",
                        "Movimentação de estoque não encontrada.", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<MovimentoEstoqueResponse> listarMovimentosRecentes() {
        return movimentoRepository.findTop10ByOrderByDataMovimentoDescIdDesc().stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EstoqueDashboardResumo montarResumo() {
        List<ItemEstoqueResumo> itens = listarItensComSaldo();
        List<ItemEstoqueResumo> criticos = itens.stream()
                .filter(ItemEstoqueResumo::ativo)
                .filter(ItemEstoqueResumo::estoqueBaixo)
                .toList();
        List<LoteEstoqueResumo> proximos = listarLotesProximosVencimento(30);
        BigDecimal valorEstimado = itens.stream()
                .map(this::valorEstimado)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        return new EstoqueDashboardResumo(
                itens.stream().filter(ItemEstoqueResumo::ativo).count(),
                criticos.size(),
                proximos.size(),
                valorEstimado,
                criticos,
                proximos,
                listarMovimentosRecentes());
    }

    @Transactional(readOnly = true)
    public List<LoteEstoqueResumo> listarLotesVencidos() {
        LocalDate hoje = LocalDate.now();
        return loteRepository.findByValidadeBeforeOrderByValidadeAsc(hoje).stream()
                .map(this::paraResumoLote)
                .filter(lote -> lote.saldo().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteEstoqueResumo> listarLotesProximosVencimento(int dias) {
        LocalDate hoje = LocalDate.now();
        return loteRepository.findByValidadeBetweenOrderByValidadeAsc(hoje, hoje.plusDays(dias)).stream()
                .map(this::paraResumoLote)
                .filter(lote -> lote.saldo().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal saldoItemTotal(Long itemId) {
        return movimentoRepository.findByItemId(itemId).stream()
                .map(this::efeitoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal saldoItemLocal(Long itemId, Long localId) {
        return movimentoRepository.findByItemId(itemId).stream()
                .map(movimento -> efeitoLocal(movimento, localId))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal ultimoPreco(Long itemId) {
        return movimentoRepository.findByItemIdOrderByDataMovimentoDescIdDesc(itemId).stream()
                .filter(movimento -> movimento.getTipo() == TipoMovimentoEstoque.ENTRADA)
                .map(MovimentoEstoque::getCustoUnitario)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public BigDecimal custoMedio(Long itemId) {
        List<MovimentoEstoque> entradas = movimentoRepository.findByItemId(itemId).stream()
                .filter(movimento -> movimento.getTipo() == TipoMovimentoEstoque.ENTRADA)
                .filter(movimento -> movimento.getCustoTotal() != null)
                .toList();
        BigDecimal quantidade = entradas.stream()
                .map(MovimentoEstoque::getQuantidade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (quantidade.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal total = entradas.stream()
                .map(MovimentoEstoque::getCustoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(quantidade, ESCALA, RoundingMode.HALF_UP);
    }

    private ItemEstoqueResumo paraResumoItem(ItemEstoque item) {
        BigDecimal saldo = saldoItemTotal(item.getId());
        BigDecimal minimo = item.getEstoqueMinimo();
        boolean baixo = item.isAtivo()
                && minimo != null
                && minimo.compareTo(BigDecimal.ZERO) > 0
                && saldo.compareTo(minimo) < 0;
        return new ItemEstoqueResumo(
                item.getId(),
                item.getNome(),
                item.getCategoria().getNome(),
                item.getUnidadeMedida().getSigla(),
                saldo,
                minimo,
                item.isAtivo(),
                baixo,
                ultimoPreco(item.getId()),
                custoMedio(item.getId()));
    }

    private LoteEstoqueResumo paraResumoLote(LoteEstoque lote) {
        return new LoteEstoqueResumo(
                lote.getId(),
                lote.getItem().getNome(),
                lote.getCodigo(),
                lote.getValidade(),
                saldoLoteTotal(lote.getItem().getId(), lote.getId()),
                lote.getItem().getUnidadeMedida().getSigla());
    }

    private MovimentoEstoqueResponse paraResponse(MovimentoEstoque movimento) {
        return new MovimentoEstoqueResponse(
                movimento.getId(),
                movimento.getItem().getId(),
                movimento.getItem().getNome(),
                movimento.getTipo(),
                movimento.getTipo().getRotulo(),
                movimento.getQuantidade(),
                movimento.getItem().getUnidadeMedida().getSigla(),
                movimento.getLocalOrigem() == null ? null : movimento.getLocalOrigem().getNome(),
                movimento.getLocalDestino() == null ? null : movimento.getLocalDestino().getNome(),
                movimento.getLote() == null ? null : movimento.getLote().getCodigo(),
                movimento.getCustoUnitario(),
                movimento.getCustoTotal(),
                movimento.getObservacao(),
                movimento.getDataMovimento(),
                movimento.getCriadoPor(),
                movimento.getOrigemModulo(),
                movimento.getOrigemReferenciaId(),
                movimento.getOrigemDescricao());
    }

    private MovimentoEstoqueRequest copiarComoEntrada(MovimentoEstoqueRequest request) {
        MovimentoEstoqueRequest entrada = new MovimentoEstoqueRequest();
        entrada.setItemId(request.getItemId());
        entrada.setTipo(TipoMovimentoEstoque.ENTRADA);
        entrada.setQuantidade(request.getQuantidade());
        entrada.setLocalDestinoId(request.getLocalDestinoId());
        entrada.setLoteCodigo(request.getLoteCodigo());
        entrada.setValidade(request.getValidade());
        entrada.setCustoUnitario(request.getCustoUnitario());
        entrada.setCustoTotal(request.getCustoTotal());
        entrada.setObservacao(request.getObservacao());
        entrada.setDataMovimento(request.getDataMovimento());
        return entrada;
    }

    private BigDecimal valorEstimado(ItemEstoqueResumo item) {
        if (item.custoMedio() == null || item.saldo().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return item.saldo().multiply(item.custoMedio());
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new EstoqueOperacaoException("QUANTIDADE_INVALIDA", "Quantidade deve ser maior que zero.");
        }
    }

    private void validarSaldoDisponivel(ItemEstoque item, LocalEstoque origem, LoteEstoque lote, BigDecimal quantidade) {
        BigDecimal saldoLocal = lote == null
                ? saldoItemLocal(item.getId(), origem.getId())
                : saldoLoteLocal(item.getId(), lote.getId(), origem.getId());
        if (saldoLocal.compareTo(quantidade) < 0) {
            try (MdcScope ignored = MdcScope.with(Map.of(
                    "event.action", "estoque.saldo_insuficiente",
                    "module", "estoque",
                    "estoque.item.id", safeId(item.getId()),
                    "estoque.local.id", safeId(origem.getId())))) {
                log.warn("Saldo de estoque insuficiente para a movimentação.");
            }
            throw new EstoqueOperacaoException("ESTOQUE_INSUFICIENTE",
                    "Saldo insuficiente para a movimentação.");
        }
    }

    private LocalEstoque resolverOrigem(TipoMovimentoEstoque tipo, MovimentoEstoqueRequest request) {
        if (!tipo.reduzOrigem()) {
            if (request.getLocalOrigemId() != null) {
                throw new EstoqueOperacaoException("LOCAL_ORIGEM_INDEVIDO",
                        "Este tipo de movimentação não deve informar local de origem.");
            }
            return null;
        }
        return buscarLocalAtivo(request.getLocalOrigemId(), "LOCAL_ORIGEM_OBRIGATORIO",
                "Informe o local de origem.");
    }

    private LocalEstoque resolverDestino(TipoMovimentoEstoque tipo, MovimentoEstoqueRequest request) {
        if (!tipo.aumentaDestino()) {
            if (request.getLocalDestinoId() != null) {
                throw new EstoqueOperacaoException("LOCAL_DESTINO_INDEVIDO",
                        "Este tipo de movimentação não deve informar local de destino.");
            }
            return null;
        }
        LocalEstoque destino = buscarLocalAtivo(request.getLocalDestinoId(), "LOCAL_DESTINO_OBRIGATORIO",
                "Informe o local de destino.");
        if (tipo == TipoMovimentoEstoque.TRANSFERENCIA
                && Objects.equals(request.getLocalOrigemId(), request.getLocalDestinoId())) {
            throw new EstoqueOperacaoException("TRANSFERENCIA_MESMO_LOCAL",
                    "Origem e destino da transferência devem ser diferentes.");
        }
        return destino;
    }

    private LoteEstoque resolverLote(ItemEstoque item, TipoMovimentoEstoque tipo, MovimentoEstoqueRequest request) {
        boolean informouLote = StringUtils.hasText(request.getLoteCodigo());
        if (!item.isControlaLote()) {
            if (informouLote || request.getValidade() != null) {
                throw new EstoqueOperacaoException("ITEM_NAO_CONTROLA_LOTE",
                        "Este item não controla lote ou validade.");
            }
            return null;
        }
        if (!informouLote) {
            throw new EstoqueOperacaoException("LOTE_OBRIGATORIO",
                    "Informe o lote para este item.");
        }

        String codigo = request.getLoteCodigo().trim();
        return loteRepository.findByItemIdAndCodigoIgnoreCase(item.getId(), codigo)
                .map(lote -> validarLoteExistente(item, lote, request))
                .orElseGet(() -> criarLoteQuandoPermitido(item, tipo, codigo, request));
    }

    private LoteEstoque validarLoteExistente(ItemEstoque item, LoteEstoque lote, MovimentoEstoqueRequest request) {
        if (request.getValidade() != null
                && lote.getValidade() != null
                && !request.getValidade().equals(lote.getValidade())) {
            throw new EstoqueOperacaoException("LOTE_VALIDADE_DIVERGENTE",
                    "A validade informada diverge da validade cadastrada para o lote.");
        }
        if (item.isControlaValidade() && lote.getValidade() == null && request.getValidade() == null) {
            throw new EstoqueOperacaoException("VALIDADE_OBRIGATORIA",
                    "Informe a validade para este lote.");
        }
        if (item.isControlaValidade() && lote.getValidade() == null) {
            lote.setValidade(request.getValidade());
            return loteRepository.save(lote);
        }
        return lote;
    }

    private LoteEstoque criarLoteQuandoPermitido(ItemEstoque item, TipoMovimentoEstoque tipo, String codigo,
            MovimentoEstoqueRequest request) {
        if (tipo.reduzOrigem()) {
            throw new EstoqueOperacaoException("LOTE_NAO_ENCONTRADO",
                    "Lote não encontrado para saída ou transferência.");
        }
        if (item.isControlaValidade() && request.getValidade() == null) {
            throw new EstoqueOperacaoException("VALIDADE_OBRIGATORIA",
                    "Informe a validade para este lote.");
        }
        LoteEstoque lote = new LoteEstoque();
        lote.setItem(item);
        lote.setCodigo(codigo);
        lote.setValidade(request.getValidade());
        return loteRepository.save(lote);
    }

    private void validarCusto(TipoMovimentoEstoque tipo, MovimentoEstoqueRequest request) {
        boolean temCusto = request.getCustoUnitario() != null || request.getCustoTotal() != null;
        if (temCusto && !tipo.entradaComCusto()) {
            throw new EstoqueOperacaoException("CUSTO_APENAS_ENTRADA",
                    "Custo deve ser informado apenas em entradas de estoque.");
        }
        if (request.getCustoUnitario() != null && request.getCustoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new EstoqueOperacaoException("CUSTO_INVALIDO", "Custo unitário não pode ser negativo.");
        }
        if (request.getCustoTotal() != null && request.getCustoTotal().compareTo(BigDecimal.ZERO) < 0) {
            throw new EstoqueOperacaoException("CUSTO_INVALIDO", "Custo total não pode ser negativo.");
        }
    }

    private void aplicarCustos(TipoMovimentoEstoque tipo, MovimentoEstoqueRequest request, MovimentoEstoque movimento) {
        if (!tipo.entradaComCusto()) {
            return;
        }
        BigDecimal custoUnitario = normalizarDecimal(request.getCustoUnitario());
        BigDecimal custoTotal = normalizarDecimal(request.getCustoTotal());
        if (custoUnitario != null && custoTotal == null) {
            custoTotal = custoUnitario.multiply(movimento.getQuantidade()).setScale(ESCALA, RoundingMode.HALF_UP);
        }
        if (custoUnitario == null && custoTotal != null) {
            custoUnitario = custoTotal.divide(movimento.getQuantidade(), ESCALA, RoundingMode.HALF_UP);
        }
        movimento.setCustoUnitario(custoUnitario);
        movimento.setCustoTotal(custoTotal);
    }

    private BigDecimal saldoLoteTotal(Long itemId, Long loteId) {
        return movimentoRepository.findByItemIdAndLoteId(itemId, loteId).stream()
                .map(this::efeitoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal saldoLoteLocal(Long itemId, Long loteId, Long localId) {
        return movimentoRepository.findByItemIdAndLoteId(itemId, loteId).stream()
                .map(movimento -> efeitoLocal(movimento, localId))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal efeitoTotal(MovimentoEstoque movimento) {
        return switch (movimento.getTipo()) {
            case ENTRADA, AJUSTE_ENTRADA -> movimento.getQuantidade();
            case CONSUMO, PERDA, AJUSTE_SAIDA, DESCARTE -> movimento.getQuantidade().negate();
            case TRANSFERENCIA -> BigDecimal.ZERO;
        };
    }

    private BigDecimal efeitoLocal(MovimentoEstoque movimento, Long localId) {
        if (movimento.getTipo().aumentaDestino()
                && movimento.getLocalDestino() != null
                && Objects.equals(movimento.getLocalDestino().getId(), localId)) {
            return movimento.getQuantidade();
        }
        if (movimento.getTipo().reduzOrigem()
                && movimento.getLocalOrigem() != null
                && Objects.equals(movimento.getLocalOrigem().getId(), localId)) {
            return movimento.getQuantidade().negate();
        }
        return BigDecimal.ZERO;
    }

    private ItemEstoque buscarItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EstoqueOperacaoException("ITEM_NAO_ENCONTRADO",
                        "Item de estoque não encontrado.", HttpStatus.NOT_FOUND));
    }

    private LocalEstoque buscarLocalAtivo(Long id, String code, String message) {
        if (id == null) {
            throw new EstoqueOperacaoException(code, message);
        }
        return localRepository.findById(id)
                .filter(LocalEstoque::isAtivo)
                .orElseThrow(() -> new EstoqueOperacaoException("LOCAL_INVALIDO",
                        "Local de estoque não encontrado ou inativo."));
    }

    private BigDecimal normalizarDecimal(BigDecimal valor) {
        return valor == null ? null : valor.setScale(ESCALA, RoundingMode.HALF_UP);
    }

    private String normalizarTextoOpcional(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }

    private String safeId(Long id) {
        return id == null ? "unknown" : String.valueOf(id);
    }
}
