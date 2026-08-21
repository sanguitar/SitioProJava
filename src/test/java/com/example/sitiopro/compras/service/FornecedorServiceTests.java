package com.example.sitiopro.compras.service;

import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.entity.Fornecedor;
import com.example.sitiopro.compras.repository.FornecedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FornecedorServiceTests {

    private FornecedorRepository fornecedorRepository;
    private FornecedorService service;

    @BeforeEach
    void setUp() {
        fornecedorRepository = mock(FornecedorRepository.class);
        service = new FornecedorService(fornecedorRepository);
    }

    @Test
    void criarNormalizaCamposESalvaFornecedorAtivo() {
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(invocation -> {
            Fornecedor fornecedor = invocation.getArgument(0);
            ReflectionTestUtils.setField(fornecedor, "id", 1L);
            return fornecedor;
        });

        FornecedorRequest request = new FornecedorRequest();
        request.setNome("  Agro Vale  ");
        request.setEmail("  compras@agrovale.test  ");
        request.setAtivo(true);

        var resumo = service.criar(request);

        assertThat(resumo.id()).isEqualTo(1L);
        assertThat(resumo.nome()).isEqualTo("Agro Vale");
        assertThat(resumo.email()).isEqualTo("compras@agrovale.test");
        assertThat(resumo.ativo()).isTrue();
    }

    @Test
    void criarRejeitaFornecedorDuplicado() {
        when(fornecedorRepository.existsByNomeIgnoreCase("Agro Vale")).thenReturn(true);

        FornecedorRequest request = new FornecedorRequest();
        request.setNome("Agro Vale");

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("FORNECEDOR_DUPLICADO");
    }

    @Test
    void atualizarPermiteDesativarFornecedorSemExcluirHistorico() {
        Fornecedor fornecedor = new Fornecedor();
        ReflectionTestUtils.setField(fornecedor, "id", 1L);
        fornecedor.setNome("Agro Vale");
        fornecedor.setAtivo(true);
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.save(any(Fornecedor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FornecedorRequest request = new FornecedorRequest();
        request.setNome("Agro Vale Atualizada");
        request.setDocumento("DOC-1");
        request.setAtivo(false);

        var resumo = service.atualizar(1L, request);

        assertThat(resumo.nome()).isEqualTo("Agro Vale Atualizada");
        assertThat(resumo.documento()).isEqualTo("DOC-1");
        assertThat(resumo.ativo()).isFalse();
    }

    @Test
    void atualizarRejeitaNomeDuplicadoDeOutroFornecedor() {
        Fornecedor fornecedor = new Fornecedor();
        ReflectionTestUtils.setField(fornecedor, "id", 1L);
        fornecedor.setNome("Agro Vale");
        when(fornecedorRepository.findById(1L)).thenReturn(Optional.of(fornecedor));
        when(fornecedorRepository.existsByNomeIgnoreCaseAndIdNot("Outra Agro", 1L)).thenReturn(true);

        FornecedorRequest request = new FornecedorRequest();
        request.setNome("Outra Agro");

        assertThatThrownBy(() -> service.atualizar(1L, request))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("FORNECEDOR_DUPLICADO");
    }

    @Test
    void listarAtivosMapeiaResumoSemEntidade() {
        Fornecedor fornecedor = new Fornecedor();
        ReflectionTestUtils.setField(fornecedor, "id", 1L);
        fornecedor.setNome("Agro Vale");
        fornecedor.setAtivo(true);
        when(fornecedorRepository.findByAtivoTrueOrderByNomeAsc()).thenReturn(List.of(fornecedor));

        assertThat(service.listarAtivos()).extracting("nome").containsExactly("Agro Vale");
    }

    @Test
    void detalharFornecedorInexistenteRetornaNotFound() {
        when(fornecedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detalhar(99L))
                .isInstanceOf(ComprasOperacaoException.class)
                .extracting("code")
                .isEqualTo("FORNECEDOR_NAO_ENCONTRADO");
    }
}
