package com.example.sitiopro.producao.service;

import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueOperacaoException;
import com.example.sitiopro.producao.dto.ProducaoForm;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProducaoServiceTests {

    @Mock
    private ProducaoRepository producaoRepository;

    @Mock
    private EstoqueCatalogoService estoqueCatalogoService;

    @InjectMocks
    private ProducaoService producaoService;

    @Test
    void salvaCadastroNovoComCategoriaAtivaCarregadaNoBackend() {
        CategoriaEstoque categoria = categoria(2L, "Grãos", true);
        ProducaoForm form = form(2L);
        when(estoqueCatalogoService.buscarCategoriaAtiva(2L)).thenReturn(categoria);
        when(producaoRepository.save(any(Producao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producao salva = producaoService.salvar(form);

        assertThat(salva.getCategoriaEstoque()).isSameAs(categoria);
        assertThat(salva.getItem()).isEqualTo("Milho em grão");
        assertThat(salva.getQuantidade()).isEqualTo(30);
        verify(estoqueCatalogoService).buscarCategoriaAtiva(2L);
    }

    @Test
    void rejeitaCategoriaInexistenteOuInativaSemPersistir() {
        ProducaoForm form = form(999L);
        when(estoqueCatalogoService.buscarCategoriaAtiva(999L)).thenThrow(new EstoqueOperacaoException(
                "CATEGORIA_INVALIDA", "Categoria de estoque não encontrada ou inativa."));

        assertThatThrownBy(() -> producaoService.salvar(form))
                .isInstanceOf(EstoqueOperacaoException.class)
                .hasMessage("Categoria de estoque não encontrada ou inativa.");
    }

    @Test
    void formularioDeEdicaoExpoeCategoriaPersistidaComoIdSelecionado() {
        CategoriaEstoque categoria = categoria(2L, "Grãos", true);
        Producao producao = new Producao();
        ReflectionTestUtils.setField(producao, "id", 7L);
        producao.setCategoriaEstoque(categoria);
        producao.setItem("Milho");
        producao.setQuantidade(10);
        producao.setUnidade("saca");
        producao.setStatus("Estoque");
        when(producaoRepository.findById(7L)).thenReturn(Optional.of(producao));

        ProducaoForm form = producaoService.formularioEdicao(7L);

        assertThat(form.getId()).isEqualTo(7L);
        assertThat(form.getCategoriaId()).isEqualTo(2L);
        assertThat(form.getItem()).isEqualTo("Milho");
    }

    private ProducaoForm form(Long categoriaId) {
        ProducaoForm form = new ProducaoForm();
        form.setCategoriaId(categoriaId);
        form.setItem(" Milho em grão ");
        form.setQuantidade(30);
        form.setUnidade(" saca ");
        form.setStatus(" Estoque ");
        return form;
    }

    private CategoriaEstoque categoria(Long id, String nome, boolean ativa) {
        CategoriaEstoque categoria = new CategoriaEstoque();
        ReflectionTestUtils.setField(categoria, "id", id);
        categoria.setNome(nome);
        categoria.setAtiva(ativa);
        return categoria;
    }
}
