package com.example.sitiopro.administracao.service;

import com.example.sitiopro.administracao.dto.AdministracaoResumo;
import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import com.example.sitiopro.observability.dto.SistemaSaudeResumo;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.shared.observability.ObservabilityProperties;
import com.example.sitiopro.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministracaoResumoServiceTests {

    @Mock private UsuarioService usuarioService;
    @Mock private SistemaSaudeService sistemaSaudeService;
    @Mock private IntegracaoPainelService integracaoPainelService;
    @Mock private ObservabilityProperties observabilityProperties;
    @InjectMocks private AdministracaoResumoService service;

    @Test
    void agregaSomenteIndicadoresSegurosDosServicosExistentes() {
        when(usuarioService.contarAtivos()).thenReturn(5L);
        when(usuarioService.contarAdministradoresAtivos()).thenReturn(2L);
        when(sistemaSaudeService.resumo()).thenReturn(new SistemaSaudeResumo(
                "UP", "UP", Duration.ofHours(1), "1.0", "test", "DISPONIVEL", "request"));
        when(integracaoPainelService.resumo()).thenReturn(new IntegracaoPainelResumo(3, 1, 0, 2, Map.of()));
        when(observabilityProperties.isEnabled()).thenReturn(true);

        AdministracaoResumo resumo = service.resumo();

        assertThat(resumo.usuarios().ativos()).isEqualTo(5);
        assertThat(resumo.usuarios().administradores()).isEqualTo(2);
        assertThat(resumo.saude().aplicacao()).isEqualTo("UP");
        assertThat(resumo.saude().banco()).isEqualTo("UP");
        assertThat(resumo.integracoes().operacionais()).isEqualTo(3);
        assertThat(resumo.integracoes().comFalha()).isEqualTo(2);
        assertThat(resumo.observabilidade().aplicavel()).isTrue();
        assertThat(resumo.observabilidade().status()).isEqualTo("DISPONIVEL");
    }

    @Test
    void degradaCadaSecaoSemImpedirResumoAdministrativo() {
        when(usuarioService.contarAtivos()).thenThrow(new IllegalStateException("usuarios indisponíveis"));
        when(sistemaSaudeService.resumo()).thenThrow(new IllegalStateException("health indisponível"));
        when(integracaoPainelService.resumo()).thenThrow(new IllegalStateException("integração indisponível"));
        when(observabilityProperties.isEnabled()).thenReturn(true);

        AdministracaoResumo resumo = service.resumo();

        assertThat(resumo.usuarios().disponivel()).isFalse();
        assertThat(resumo.saude().disponivel()).isFalse();
        assertThat(resumo.integracoes().disponivel()).isFalse();
        assertThat(resumo.observabilidade().aplicavel()).isTrue();
        assertThat(resumo.observabilidade().status()).isEqualTo("INDISPONIVEL");
    }
}
