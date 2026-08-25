package com.example.sitiopro.abastecimento.service;

import com.example.sitiopro.abastecimento.model.Abastecimento;
import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.frota.model.Veiculo;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.frota.service.VeiculoService;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbastecimentoServiceTests {

    @Mock private AbastecimentoRepository abastecimentoRepository;
    @Mock private VeiculoService veiculoService;
    private AbastecimentoService service;

    @BeforeEach
    void setUp() {
        service = new AbastecimentoService(abastecimentoRepository, veiculoService);
    }

    @Test
    void abastecimentoAtualizaHodometroComVeiculoBloqueado() throws Exception {
        Veiculo veiculo = new Veiculo();
        ReflectionTestUtils.setField(veiculo, "id", 1L);
        veiculo.setKmAtual(100D);
        Abastecimento abastecimento = new Abastecimento();
        abastecimento.setVeiculo(veiculo);
        abastecimento.setKmNoAto(150D);

        when(veiculoService.buscarParaAtualizacao(1L)).thenReturn(veiculo);
        when(abastecimentoRepository.save(abastecimento)).thenReturn(abastecimento);

        assertThat(service.registrarAbastecimento(abastecimento).getVeiculo().getKmAtual()).isEqualTo(150D);
        verify(veiculoService).salvar(veiculo);

        Transactional transactional = VeiculoService.class
                .getMethod("buscarParaAtualizacao", Long.class)
                .getAnnotation(Transactional.class);
        assertThat(transactional.propagation()).isEqualTo(Propagation.MANDATORY);
        Lock lock = VeiculoRepository.class.getMethod("buscarParaAtualizacao", Long.class)
                .getAnnotation(Lock.class);
        assertThat(lock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }
}
