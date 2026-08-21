package com.example.sitiopro.compras.service;

import com.example.sitiopro.compras.dto.CompraDetalhe;
import com.example.sitiopro.compras.dto.CompraRequest;
import com.example.sitiopro.compras.dto.FornecedorResumo;
import com.example.sitiopro.compras.dto.ItemCompraRequest;
import com.example.sitiopro.compras.entity.Compra;
import com.example.sitiopro.compras.entity.Fornecedor;
import com.example.sitiopro.compras.entity.ItemCompra;
import com.example.sitiopro.compras.entity.StatusCompra;
import com.example.sitiopro.compras.repository.CompraRepository;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.estoque.entity.UnidadeMedida;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CompraServiceTests {

    private CompraRepository compraRepository;
    private FornecedorService fornecedorService;
    private EstoqueCatalogoService estoqueCatalogoService;
    private EstoqueMovimentoService estoqueMovimentoService;
    private CompraService service;

    private Fornecedor fornecedor;
    private ItemEstoque racao;
    private ItemEstoque vacina;
    private LocalEstoque deposito;

    @BeforeEach
    void setUp() {
        compraRepository = mock(CompraRepository.class);
        fornecedorService = mock(FornecedorService.class);
        estoqueCatalogoService = mock(EstoqueCatalogoService.class);
        estoqueMovimentoService = mock(EstoqueMovimentoService.class);
        service = new CompraService(compraRepository, fornecedorService, estoqueCatalogoService,
                estoqueMovimentoService);

        fornecedor = fornecedor(1L, "Agro Vale", true);
        racao = item(10L, "Ração postura", true, false, false);
        vacina = item(11L, "Vacina", true, true, true);
        deposito = local(20L, "Deposito", true);

        lenient().when(fornecedorService.paraResumo(any(Fornecedor.class))).thenAnswer(invocation -> {
            Fornecedor origem = invocation.getArgument(0);
            return new FornecedorResumo(origem.getId(), origem.getNome(), origem.getDocumento(),
                    origem.getTelefone(), origem.getEmail(), origem.isAtivo());
        });
        lenient().when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> {
            Compra compra = invocation.getArgument(0);
            if (compra.getId() == null) {
                ReflectionTestUtils.setField(compra, "id", 100L);
            }
            return compra;
        });
        lenient().when(estoqueCatalogoService.buscarItem(10L)).thenReturn(racao);
        lenient().when(estoqueCatalogoService.buscarItem(11L)).thenReturn(vacina);
        lenient().when(estoqueCatalogoService.buscarLocalAtivo(20L)).thenReturn(deposito);
    }

    @Test
    void criarCompraIniciaRascunhoECalculaTotalDoCabecalho() {
        when(fornecedorService.buscarAtivoPorId(1L)).thenReturn(fornecedor);

        CompraRequest request = compraRequest();
        request.setFrete(new BigDecimal("12.50"));
        request.setDesconto(new BigDecimal("2.50"));

        CompraDetalhe detalhe = service.criarCompra(request);

        assertThat(detalhe.status()).isEqualTo(StatusCompra.RASCUNHO);
        assertThat(detalhe.subtotal()).isEqualByComparingTo("0");
        assertThat(detalhe.total()).isEqualByComparingTo("10.0000");
    }

    @Test
    void adicionarItemRecalculaSubtotalETotalNoServidor() {
        Compra compra = compra(100L, StatusCompra.RASCUNHO, fornecedor);
        compra.setFrete(new BigDecimal("5"));
        compra.setDesconto(new BigDecimal("1"));
        when(compraRepository.findById(100L)).thenReturn(Optional.of(compra));

        ItemCompraRequest request = itemRequest(10L, "2.5", "4.00", 20L);

        CompraDetalhe detalhe = service.adicionarItem(100L, request);

        assertThat(detalhe.itens()).hasSize(1);
        assertThat(detalhe.subtotal()).isEqualByComparingTo("10.0000");
        assertThat(detalhe.total()).isEqualByComparingTo("14.0000");
    }

    @Test
    void atualizarItemDeRascunhoRecalculaValoresNoServidor() {
        Compra compra = compra(100L, StatusCompra.RASCUNHO, fornecedor);
        compra.setFrete(new BigDecimal("5"));
        compra.setDesconto(new BigDecimal("1"));
        compra.adicionarItem(itemCompra(501L, racao, deposito, "2", "4.00", null, null));
        when(compraRepository.findById(100L)).thenReturn(Optional.of(compra));

        CompraDetalhe detalhe = service.atualizarItem(100L, 501L, itemRequest(10L, "3", "5.00", 20L));

        assertThat(detalhe.itens()).hasSize(1);
        assertThat(detalhe.itens().getFirst().quantidade()).isEqualByComparingTo("3.0000");
        assertThat(detalhe.itens().getFirst().custoUnitario()).isEqualByComparingTo("5.0000");
        assertThat(detalhe.subtotal()).isEqualByComparingTo("15.0000");
        assertThat(detalhe.total()).isEqualByComparingTo("19.0000");
    }

    @Test
    void confirmarSemItensERejeitado() {
        Compra compra = compra(100L, StatusCompra.RASCUNHO, fornecedor);
        when(compraRepository.buscarParaConfirmacao(100L)).thenReturn(Optional.of(compra));

        assertThatThrownBy(() -> service.confirmarCompra(100L))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("COMPRA_SEM_ITENS");
        verify(estoqueMovimentoService, never()).registrarEntradaCompra(any(), any());
    }

    @Test
    void fornecedorInativoImpedeConfirmacao() {
        fornecedor.setAtivo(false);
        Compra compra = compraComItem(StatusCompra.RASCUNHO, racao, deposito);
        when(compraRepository.buscarParaConfirmacao(100L)).thenReturn(Optional.of(compra));

        assertThatThrownBy(() -> service.confirmarCompra(100L))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("FORNECEDOR_INATIVO");
        verify(estoqueMovimentoService, never()).registrarEntradaCompra(any(), any());
    }

    @Test
    void itemInativoImpedeConfirmacao() {
        racao.setAtivo(false);
        Compra compra = compraComItem(StatusCompra.RASCUNHO, racao, deposito);
        when(compraRepository.buscarParaConfirmacao(100L)).thenReturn(Optional.of(compra));

        assertThatThrownBy(() -> service.confirmarCompra(100L))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("ITEM_ESTOQUE_INATIVO");
        verify(estoqueMovimentoService, never()).registrarEntradaCompra(any(), any());
    }

    @Test
    void itemComControleDeValidadeExigeLoteEValidade() {
        Compra compra = compra(100L, StatusCompra.RASCUNHO, fornecedor);
        when(compraRepository.findById(100L)).thenReturn(Optional.of(compra));

        ItemCompraRequest request = itemRequest(11L, "1", "7.00", 20L);
        request.setLoteCodigo("L-001");

        assertThatThrownBy(() -> service.adicionarItem(100L, request))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("VALIDADE_OBRIGATORIA");
    }

    @Test
    void confirmarCompraCriaEntradaEstoqueComCustoLoteValidadeEVinculo() {
        Compra compra = compra(100L, StatusCompra.RASCUNHO, fornecedor);
        compra.adicionarItem(itemCompra(501L, racao, deposito, "2", "3.25", null, null));
        compra.adicionarItem(itemCompra(502L, vacina, deposito, "1", "7.00", "L-001", LocalDate.of(2026, 9, 30)));
        when(compraRepository.buscarParaConfirmacao(100L)).thenReturn(Optional.of(compra));
        when(estoqueMovimentoService.registrarEntradaCompra(any(), eq(100L)))
                .thenReturn(movimento(900L), movimento(901L));

        CompraDetalhe detalhe = service.confirmarCompra(100L);

        assertThat(detalhe.status()).isEqualTo(StatusCompra.CONFIRMADA);
        assertThat(detalhe.confirmadoPor()).isNotBlank();
        assertThat(detalhe.itens()).extracting("movimentoEstoqueId").containsExactly(900L, 901L);

        ArgumentCaptor<MovimentoEstoqueRequest> captor = ArgumentCaptor.forClass(MovimentoEstoqueRequest.class);
        verify(estoqueMovimentoService, times(2)).registrarEntradaCompra(captor.capture(), eq(100L));
        assertThat(captor.getAllValues().getFirst().getCustoTotal()).isEqualByComparingTo("6.5000");
        assertThat(captor.getAllValues().get(1).getLoteCodigo()).isEqualTo("L-001");
        assertThat(captor.getAllValues().get(1).getValidade()).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    @Test
    void confirmarDuasVezesNaoDuplicaEntrada() {
        Compra compra = compraComItem(StatusCompra.RASCUNHO, racao, deposito);
        when(compraRepository.buscarParaConfirmacao(100L)).thenReturn(Optional.of(compra));
        when(estoqueMovimentoService.registrarEntradaCompra(any(), eq(100L))).thenReturn(movimento(900L));

        service.confirmarCompra(100L);

        assertThatThrownBy(() -> service.confirmarCompra(100L))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("COMPRA_JA_CONFIRMADA");
        verify(estoqueMovimentoService, times(1)).registrarEntradaCompra(any(), eq(100L));
    }

    @Test
    void compraConfirmadaNaoAceitaEdicao() {
        Compra compra = compraComItem(StatusCompra.CONFIRMADA, racao, deposito);
        when(compraRepository.findById(100L)).thenReturn(Optional.of(compra));

        assertThatThrownBy(() -> service.adicionarItem(100L, itemRequest(10L, "1", "1", 20L)))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("COMPRA_JA_CONFIRMADA");
    }

    @Test
    void compraCanceladaNaoConfirma() {
        Compra compra = compraComItem(StatusCompra.CANCELADA, racao, deposito);
        when(compraRepository.buscarParaConfirmacao(100L)).thenReturn(Optional.of(compra));

        assertThatThrownBy(() -> service.confirmarCompra(100L))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("COMPRA_CANCELADA");
    }

    @Test
    void cancelarRascunhoMarcaCompraSemGerarMovimento() {
        Compra compra = compraComItem(StatusCompra.RASCUNHO, racao, deposito);
        when(compraRepository.findById(100L)).thenReturn(Optional.of(compra));

        CompraDetalhe detalhe = service.cancelarRascunho(100L);

        assertThat(detalhe.status()).isEqualTo(StatusCompra.CANCELADA);
        verify(estoqueMovimentoService, never()).registrarEntradaCompra(any(), any());
    }

    @Test
    void falhaNoEstoquePropagaErroParaRollbackDaTransacao() {
        Compra compra = compraComItem(StatusCompra.RASCUNHO, racao, deposito);
        when(compraRepository.buscarParaConfirmacao(100L)).thenReturn(Optional.of(compra));
        when(estoqueMovimentoService.registrarEntradaCompra(any(), eq(100L)))
                .thenThrow(new EstoqueOperacaoException("ESTOQUE_FALHOU", "Falha no estoque."));

        assertThatThrownBy(() -> service.confirmarCompra(100L))
                .isInstanceOf(EstoqueOperacaoException.class)
                .extracting("code")
                .isEqualTo("ESTOQUE_FALHOU");
    }

    @Test
    void confirmarCompraETransacional() throws NoSuchMethodException {
        Transactional transactional = CompraService.class
                .getMethod("confirmarCompra", Long.class)
                .getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
    }

    @Test
    void repositoryUsaLockPessimistaNaConfirmacao() throws NoSuchMethodException {
        Lock lock = CompraRepository.class
                .getMethod("buscarParaConfirmacao", Long.class)
                .getAnnotation(Lock.class);

        assertThat(lock).isNotNull();
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    private CompraRequest compraRequest() {
        CompraRequest request = new CompraRequest();
        request.setFornecedorId(1L);
        request.setDataCompra(LocalDate.of(2026, 8, 21));
        request.setNumeroDocumento("NF-1");
        return request;
    }

    private ItemCompraRequest itemRequest(Long itemId, String quantidade, String custoUnitario, Long localId) {
        ItemCompraRequest request = new ItemCompraRequest();
        request.setItemEstoqueId(itemId);
        request.setQuantidade(new BigDecimal(quantidade));
        request.setCustoUnitario(new BigDecimal(custoUnitario));
        request.setLocalDestinoId(localId);
        return request;
    }

    private Compra compraComItem(StatusCompra status, ItemEstoque item, LocalEstoque local) {
        Compra compra = compra(100L, status, fornecedor);
        compra.adicionarItem(itemCompra(501L, item, local, "2", "3.25", null, null));
        return compra;
    }

    private Compra compra(Long id, StatusCompra status, Fornecedor fornecedor) {
        Compra compra = new Compra();
        ReflectionTestUtils.setField(compra, "id", id);
        compra.setFornecedor(fornecedor);
        compra.setDataCompra(LocalDate.of(2026, 8, 21));
        compra.setStatus(status);
        compra.setFrete(BigDecimal.ZERO);
        compra.setDesconto(BigDecimal.ZERO);
        compra.setSubtotal(BigDecimal.ZERO);
        compra.setTotal(BigDecimal.ZERO);
        return compra;
    }

    private ItemCompra itemCompra(Long id, ItemEstoque itemEstoque, LocalEstoque local,
            String quantidade, String custoUnitario, String lote, LocalDate validade) {
        ItemCompra item = new ItemCompra();
        ReflectionTestUtils.setField(item, "id", id);
        item.setItemEstoque(itemEstoque);
        item.setLocalDestino(local);
        item.setQuantidade(new BigDecimal(quantidade));
        item.setCustoUnitario(new BigDecimal(custoUnitario));
        item.setSubtotal(new BigDecimal(quantidade).multiply(new BigDecimal(custoUnitario)));
        item.setLoteCodigo(lote);
        item.setValidade(validade);
        return item;
    }

    private MovimentoEstoque movimento(Long id) {
        MovimentoEstoque movimento = new MovimentoEstoque();
        ReflectionTestUtils.setField(movimento, "id", id);
        return movimento;
    }

    private Fornecedor fornecedor(Long id, String nome, boolean ativo) {
        Fornecedor novoFornecedor = new Fornecedor();
        ReflectionTestUtils.setField(novoFornecedor, "id", id);
        novoFornecedor.setNome(nome);
        novoFornecedor.setAtivo(ativo);
        return novoFornecedor;
    }

    private ItemEstoque item(Long id, String nome, boolean ativo, boolean controlaLote, boolean controlaValidade) {
        CategoriaEstoque categoria = new CategoriaEstoque();
        ReflectionTestUtils.setField(categoria, "id", 1L);
        categoria.setNome("Geral");
        UnidadeMedida unidade = new UnidadeMedida();
        ReflectionTestUtils.setField(unidade, "id", 1L);
        unidade.setNome("Quilograma");
        unidade.setSigla("KG");
        ItemEstoque item = new ItemEstoque();
        ReflectionTestUtils.setField(item, "id", id);
        item.setNome(nome);
        item.setAtivo(ativo);
        item.setControlaLote(controlaLote);
        item.setControlaValidade(controlaValidade);
        item.setCategoria(categoria);
        item.setUnidadeMedida(unidade);
        return item;
    }

    private LocalEstoque local(Long id, String nome, boolean ativo) {
        LocalEstoque local = new LocalEstoque();
        ReflectionTestUtils.setField(local, "id", id);
        local.setNome(nome);
        local.setAtivo(ativo);
        return local;
    }
}
