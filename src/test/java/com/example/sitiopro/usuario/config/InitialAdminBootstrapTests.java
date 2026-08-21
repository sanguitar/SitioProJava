package com.example.sitiopro.usuario.config;

import com.example.sitiopro.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialAdminBootstrapTests {

    private static final String SENHA_VALIDA = "SenhaMuitoForte123";

    @Mock
    private UsuarioService usuarioService;

    private InitialAdminProperties properties;
    private InitialAdminBootstrap bootstrap;

    @BeforeEach
    void configurar() {
        properties = new InitialAdminProperties();
        bootstrap = new InitialAdminBootstrap(properties, usuarioService);
    }

    @Test
    void bootstrapDesabilitadoNaoConsultaUsuarios() {
        bootstrap.run(new DefaultApplicationArguments());

        verifyNoInteractions(usuarioService);
    }

    @Test
    void usuarioInicialExistenteNaoERecriadoNemAlterado() {
        habilitarBootstrapValido();
        when(usuarioService.existePorLogin("sanderson")).thenReturn(true);

        bootstrap.run(new DefaultApplicationArguments());

        verify(usuarioService).existePorLogin("sanderson");
        verify(usuarioService, never()).contarUsuarios();
        verify(usuarioService, never()).criarAdminInicial("Sânderson", "sanderson", SENHA_VALIDA);
    }

    @Test
    void tabelaComOutrosUsuariosNaoRecebeNovoAdminPorBootstrap() {
        habilitarBootstrapValido();
        when(usuarioService.existePorLogin("sanderson")).thenReturn(false);
        when(usuarioService.contarUsuarios()).thenReturn(1L);

        bootstrap.run(new DefaultApplicationArguments());

        verify(usuarioService, never()).criarAdminInicial("Sânderson", "sanderson", SENHA_VALIDA);
    }

    @Test
    void tabelaVaziaCriaAdminInicial() {
        habilitarBootstrapValido();
        when(usuarioService.existePorLogin("sanderson")).thenReturn(false);
        when(usuarioService.contarUsuarios()).thenReturn(0L);

        bootstrap.run(new DefaultApplicationArguments());

        verify(usuarioService).criarAdminInicial("Sânderson", "sanderson", SENHA_VALIDA);
    }

    @Test
    void senhaAusenteFalhaSomenteQuandoCriacaoSeriaNecessaria() {
        properties.setEnabled(true);
        properties.setLogin("sanderson");
        properties.setName("Sânderson");
        when(usuarioService.existePorLogin("sanderson")).thenReturn(false);
        when(usuarioService.contarUsuarios()).thenReturn(0L);

        assertThatThrownBy(() -> bootstrap.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sem senha");

        verify(usuarioService, never()).criarAdminInicial("Sânderson", "sanderson", SENHA_VALIDA);
    }

    private void habilitarBootstrapValido() {
        properties.setEnabled(true);
        properties.setLogin("sanderson");
        properties.setPassword(SENHA_VALIDA);
        properties.setName("Sânderson");
    }
}
