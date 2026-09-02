package com.example.sitiopro.usuario.security;

import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioSessaoServiceTests {

    @Test
    void revogaSomenteSessoesDoUsuarioAlvo() {
        SessionRegistryImpl registry = new SessionRegistryImpl();
        UsuarioPrincipal alvo = principal(10L, "alvo");
        UsuarioPrincipal outro = principal(20L, "outro");
        registry.registerNewSession("sessao-alvo-1", alvo);
        registry.registerNewSession("sessao-alvo-2", alvo);
        registry.registerNewSession("sessao-outro", outro);
        UsuarioSessaoService service = new UsuarioSessaoService(registry);

        int revogadas = service.revogarAgora(10L);

        assertThat(revogadas).isEqualTo(2);
        assertThat(registry.getSessionInformation("sessao-alvo-1").isExpired()).isTrue();
        assertThat(registry.getSessionInformation("sessao-alvo-2").isExpired()).isTrue();
        assertThat(registry.getSessionInformation("sessao-outro").isExpired()).isFalse();
    }

    @Test
    void aguardaCommitAntesDeExpirarSessao() {
        SessionRegistryImpl registry = new SessionRegistryImpl();
        UsuarioPrincipal alvo = principal(10L, "alvo");
        registry.registerNewSession("sessao-alvo", alvo);
        UsuarioSessaoService service = new UsuarioSessaoService(registry);

        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.revogarAposCommit(10L);

            SessionInformation sessao = registry.getSessionInformation("sessao-alvo");
            assertThat(sessao.isExpired()).isFalse();
            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager.getSynchronizations();
            assertThat(synchronizations).hasSize(1);
            synchronizations.forEach(TransactionSynchronization::afterCommit);
            assertThat(sessao.isExpired()).isTrue();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
            TransactionSynchronizationManager.setActualTransactionActive(false);
        }
    }

    private UsuarioPrincipal principal(Long id, String login) {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setNome(login);
        usuario.setLogin(login);
        usuario.setSenhaHash("{noop}senha");
        usuario.setPerfil(PerfilUsuario.OPERADOR);
        usuario.setAtivo(true);
        return new UsuarioPrincipal(usuario);
    }
}
