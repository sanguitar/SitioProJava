package com.example.sitiopro.estoque.service;

import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.entity.LocalEstoque;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import com.example.sitiopro.estoque.entity.UnidadeMedida;
import com.example.sitiopro.estoque.repository.ItemEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.LoteEstoqueRepository;
import com.example.sitiopro.estoque.repository.MovimentoEstoqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EstoqueMovimentoServiceTests {

    private ItemEstoqueRepository itemRepository;
    private LocalEstoqueRepository localRepository;
    private LoteEstoqueRepository loteRepository;
    private MovimentoEstoqueRepository movimentoRepository;
    private EstoqueMovimentoService service;

    private ItemEstoque item;
    private LocalEstoque deposito;
    private LocalEstoque galinheiro;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemEstoqueRepository.class);
        localRepository = mock(LocalEstoqueRepository.class);
        loteRepository = mock(LoteEstoqueRepository.class);
        movimentoRepository = mock(MovimentoEstoqueRepository.class);
        service = new EstoqueMovimentoService(itemRepository, localRepository, loteRepository, movimentoRepository);

        item = item(1L, "Ração postura");
        deposito = local(10L, "Depósito");
        galinheiro = local(11L, "Galinheiro");

        lenient().when(movimentoRepository.save(any(MovimentoEstoque.class))).thenAnswer(invocation -> {
            MovimentoEstoque movimento = invocation.getArgument(0);
            ReflectionTestUtils.setField(movimento, "id", 99L);
            return movimento;
        });
    }

    @Test
    void entradaAumentaSaldo() {
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito)));

        assertThat(service.saldoItemTotal(1L)).isEqualByComparingTo("100");
        assertThat(service.saldoItemLocal(1L, 10L)).isEqualByComparingTo("100");
    }

    @Test
    void consumoDiminuiSaldo() {
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito),
                movimento(TipoMovimentoEstoque.CONSUMO, "20", deposito, null)));

        assertThat(service.saldoItemTotal(1L)).isEqualByComparingTo("80");
        assertThat(service.saldoItemLocal(1L, 10L)).isEqualByComparingTo("80");
    }

    @Test
    void perdaDiminuiSaldo() {
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito),
                movimento(TipoMovimentoEstoque.PERDA, "5", deposito, null)));

        assertThat(service.saldoItemTotal(1L)).isEqualByComparingTo("95");
    }

    @Test
    void descarteDiminuiSaldo() {
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito),
                movimento(TipoMovimentoEstoque.DESCARTE, "7", deposito, null)));

        assertThat(service.saldoItemTotal(1L)).isEqualByComparingTo("93");
    }

    @Test
    void transferenciaPreservaSaldoTotal() {
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito),
                movimento(TipoMovimentoEstoque.TRANSFERENCIA, "40", deposito, galinheiro)));

        assertThat(service.saldoItemTotal(1L)).isEqualByComparingTo("100");
    }

    @Test
    void transferenciaAlteraCorretamenteOsLocais() {
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito),
                movimento(TipoMovimentoEstoque.TRANSFERENCIA, "40", deposito, galinheiro)));

        assertThat(service.saldoItemLocal(1L, 10L)).isEqualByComparingTo("60");
        assertThat(service.saldoItemLocal(1L, 11L)).isEqualByComparingTo("40");
    }

    @Test
    void estoqueNegativoERejeitado() {
        prepararMovimentoBasico();
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "10", null, deposito)));

        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.CONSUMO, "15");
        request.setLocalOrigemId(10L);

        assertThatThrownBy(() -> service.registrarMovimento(request, false))
                .isInstanceOf(EstoqueOperacaoException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    void quantidadeZeroERejeitada() {
        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.ENTRADA, "0");

        assertThatThrownBy(() -> service.registrarMovimento(request, true))
                .isInstanceOf(EstoqueOperacaoException.class)
                .hasMessageContaining("Quantidade");
    }

    @Test
    void quantidadeNegativaERejeitada() {
        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.ENTRADA, "-1");

        assertThatThrownBy(() -> service.registrarMovimento(request, true))
                .isInstanceOf(EstoqueOperacaoException.class)
                .hasMessageContaining("Quantidade");
    }

    @Test
    void itemInativoNaoAceitaMovimentacao() {
        item.setAtivo(false);
        prepararMovimentoBasico();

        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.ENTRADA, "10");
        request.setLocalDestinoId(10L);

        assertThatThrownBy(() -> service.registrarMovimento(request, true))
                .isInstanceOf(EstoqueOperacaoException.class)
                .hasMessageContaining("Item inativo");
    }

    @Test
    void estoqueMinimoIdentificaItemAbaixoDoMinimo() {
        item.setEstoqueMinimo(new BigDecimal("80"));
        when(itemRepository.findAllByOrderByNomeAsc()).thenReturn(List.of(item));
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(
                movimento(TipoMovimentoEstoque.ENTRADA, "43", null, deposito)));

        ItemEstoqueResumo resumo = service.listarItensComSaldo().getFirst();

        assertThat(resumo.estoqueBaixo()).isTrue();
        assertThat(resumo.saldo()).isEqualByComparingTo("43");
    }

    @Test
    void ultimoPrecoUsaEntradaMaisRecenteComCustoUnitario() {
        MovimentoEstoque antigo = movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito);
        antigo.setCustoUnitario(new BigDecimal("3.50"));
        MovimentoEstoque recente = movimento(TipoMovimentoEstoque.ENTRADA, "50", null, deposito);
        recente.setCustoUnitario(new BigDecimal("3.72"));
        when(movimentoRepository.findByItemIdOrderByDataMovimentoDescIdDesc(1L)).thenReturn(List.of(recente, antigo));

        assertThat(service.ultimoPreco(1L)).isEqualByComparingTo("3.72");
    }

    @Test
    void custoMedioPonderadoUsaEntradasComCustoTotal() {
        MovimentoEstoque primeira = movimento(TipoMovimentoEstoque.ENTRADA, "100", null, deposito);
        primeira.setCustoTotal(new BigDecimal("350"));
        MovimentoEstoque segunda = movimento(TipoMovimentoEstoque.ENTRADA, "50", null, deposito);
        segunda.setCustoTotal(new BigDecimal("200"));
        when(movimentoRepository.findByItemId(1L)).thenReturn(List.of(primeira, segunda));

        assertThat(service.custoMedio(1L)).isEqualByComparingTo("3.6667");
    }

    @Test
    void loteObrigatorioQuandoItemControlaLote() {
        item.setControlaLote(true);
        prepararMovimentoBasico();

        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.ENTRADA, "10");
        request.setLocalDestinoId(10L);

        assertThatThrownBy(() -> service.registrarMovimento(request, true))
                .isInstanceOf(EstoqueOperacaoException.class)
                .hasMessageContaining("lote");
    }

    @Test
    void validadeObrigatoriaQuandoItemControlaValidade() {
        item.setControlaLote(true);
        item.setControlaValidade(true);
        prepararMovimentoBasico();
        when(loteRepository.findByItemIdAndCodigoIgnoreCase(1L, "L1")).thenReturn(Optional.empty());

        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.ENTRADA, "10");
        request.setLocalDestinoId(10L);
        request.setLoteCodigo("L1");

        assertThatThrownBy(() -> service.registrarMovimento(request, true))
                .isInstanceOf(EstoqueOperacaoException.class)
                .hasMessageContaining("validade");
    }

    @Test
    void entradaComCustoUnitarioCalculaCustoTotal() {
        prepararMovimentoBasico();

        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.ENTRADA, "10");
        request.setLocalDestinoId(10L);
        request.setCustoUnitario(new BigDecimal("3.72"));

        assertThat(service.registrarMovimento(request, true).custoTotal()).isEqualByComparingTo("37.20");
    }

    @Test
    void ajusteAdministrativoRejeitaOperador() {
        prepararMovimentoBasico();

        MovimentoEstoqueRequest request = request(TipoMovimentoEstoque.AJUSTE_SAIDA, "1");
        request.setLocalOrigemId(10L);

        assertThatThrownBy(() -> service.registrarMovimento(request, false))
                .isInstanceOf(EstoqueOperacaoException.class)
                .extracting("code")
                .isEqualTo("AJUSTE_RESTRITO");
    }

    @Test
    void registrarMovimentoETransacionalParaTransferenciaAtomica() throws NoSuchMethodException {
        Transactional transactional = EstoqueMovimentoService.class
                .getMethod("registrarMovimento", MovimentoEstoqueRequest.class, boolean.class)
                .getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
    }

    private void prepararMovimentoBasico() {
        lenient().when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        lenient().when(localRepository.findById(10L)).thenReturn(Optional.of(deposito));
        lenient().when(localRepository.findById(11L)).thenReturn(Optional.of(galinheiro));
    }

    private MovimentoEstoqueRequest request(TipoMovimentoEstoque tipo, String quantidade) {
        MovimentoEstoqueRequest request = new MovimentoEstoqueRequest();
        request.setItemId(1L);
        request.setTipo(tipo);
        request.setQuantidade(new BigDecimal(quantidade));
        return request;
    }

    private MovimentoEstoque movimento(TipoMovimentoEstoque tipo, String quantidade, LocalEstoque origem,
            LocalEstoque destino) {
        MovimentoEstoque movimento = new MovimentoEstoque();
        ReflectionTestUtils.setField(movimento, "id", 1L);
        movimento.setItem(item);
        movimento.setTipo(tipo);
        movimento.setQuantidade(new BigDecimal(quantidade));
        movimento.setLocalOrigem(origem);
        movimento.setLocalDestino(destino);
        return movimento;
    }

    private ItemEstoque item(Long id, String nome) {
        CategoriaEstoque categoria = new CategoriaEstoque();
        ReflectionTestUtils.setField(categoria, "id", 1L);
        categoria.setNome("Geral");
        UnidadeMedida unidade = new UnidadeMedida();
        ReflectionTestUtils.setField(unidade, "id", 1L);
        unidade.setNome("Quilograma");
        unidade.setSigla("KG");
        ItemEstoque novoItem = new ItemEstoque();
        ReflectionTestUtils.setField(novoItem, "id", id);
        novoItem.setNome(nome);
        novoItem.setCategoria(categoria);
        novoItem.setUnidadeMedida(unidade);
        novoItem.setAtivo(true);
        return novoItem;
    }

    private LocalEstoque local(Long id, String nome) {
        LocalEstoque local = new LocalEstoque();
        ReflectionTestUtils.setField(local, "id", id);
        local.setNome(nome);
        local.setAtivo(true);
        return local;
    }
}
