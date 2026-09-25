package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import com.example.sitiopro.criacao.aves.repository.LoteAvesRepository;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoRequest;
import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import com.example.sitiopro.criacao.core.repository.InstalacaoCriacaoRepository;
import com.example.sitiopro.criacao.suinos.repository.LoteSuinosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstalacaoCriacaoServiceTests {
    @Mock private InstalacaoCriacaoRepository repository;
    @Mock private LoteAvesRepository loteRepository;
    @Mock private LoteSuinosRepository loteSuinosRepository;
    @Mock private ObjectProvider<LoteSuinosRepository> loteSuinosProvider;
    private InstalacaoCriacaoService service;

    @BeforeEach
    void preparar() {
        lenient().when(loteSuinosRepository.somarOcupacao(any(), any(), any())).thenReturn(0L);
        lenient().when(loteSuinosProvider.getIfAvailable()).thenReturn(loteSuinosRepository);
        service = new InstalacaoCriacaoService(repository, loteRepository, loteSuinosProvider,
                org.mockito.Mockito.mock(com.example.sitiopro.propriedade.service.PropriedadeService.class));
    }

    @Test
    void criaInstalacaoAtivaComCapacidade() {
        when(repository.save(any())).thenAnswer(invocation -> {
            InstalacaoCriacao instalacao = invocation.getArgument(0);
            ReflectionTestUtils.setField(instalacao, "id", 1L);
            return instalacao;
        });
        when(loteRepository.somarOcupacao(1L, StatusLoteAves.ATIVO, null)).thenReturn(0L);

        var criada = service.criar(request(" Galinheiro Norte ", 120, true));

        assertThat(criada.nome()).isEqualTo("Galinheiro Norte");
        assertThat(criada.capacidade()).isEqualTo(120);
        assertThat(criada.ativo()).isTrue();
    }

    @Test
    void editaInstalacaoQuandoCapacidadeComportaOcupacao() {
        InstalacaoCriacao atual = instalacao(1L, 100, true);
        when(repository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(atual));
        when(loteRepository.somarOcupacao(1L, StatusLoteAves.ATIVO, null)).thenReturn(70L);

        var atualizada = service.atualizar(1L, request("Galinheiro reformado", 80, true));

        assertThat(atualizada.nome()).isEqualTo("Galinheiro reformado");
        assertThat(atualizada.capacidade()).isEqualTo(80);
    }

    @Test
    void naoInativaInstalacaoComLoteAtivo() {
        when(repository.buscarParaAtualizacao(1L)).thenReturn(Optional.of(instalacao(1L, 100, true)));
        when(loteRepository.somarOcupacao(1L, StatusLoteAves.ATIVO, null)).thenReturn(15L);

        assertThatThrownBy(() -> service.atualizar(1L, request("Galinheiro", 100, false)))
                .isInstanceOf(AvesOperacaoException.class)
                .extracting("code").isEqualTo("INSTALACAO_OCUPADA");
    }

    @Test
    void capacidadeRigidaImpedeExcesso() {
        InstalacaoCriacao destino = instalacao(2L, 100, true);
        when(loteRepository.somarOcupacao(2L, StatusLoteAves.ATIVO, 9L)).thenReturn(90L);

        assertThatThrownBy(() -> service.validarCapacidade(destino, 20, 9L))
                .isInstanceOf(AvesOperacaoException.class)
                .extracting("code").isEqualTo("CAPACIDADE_EXCEDIDA");
    }

    private InstalacaoCriacaoRequest request(String nome, Integer capacidade, boolean ativa) {
        InstalacaoCriacaoRequest request = new InstalacaoCriacaoRequest();
        request.setNome(nome);
        request.setTipo(TipoInstalacaoCriacao.GALINHEIRO);
        request.setCapacidade(capacidade);
        request.setAtivo(ativa);
        return request;
    }

    private InstalacaoCriacao instalacao(Long id, Integer capacidade, boolean ativa) {
        InstalacaoCriacao instalacao = new InstalacaoCriacao();
        ReflectionTestUtils.setField(instalacao, "id", id);
        instalacao.setNome("Galinheiro");
        instalacao.setTipo(TipoInstalacaoCriacao.GALINHEIRO);
        instalacao.setCapacidade(capacidade);
        instalacao.setAtivo(ativa);
        return instalacao;
    }
}
