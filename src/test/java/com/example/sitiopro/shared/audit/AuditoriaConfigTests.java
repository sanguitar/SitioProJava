package com.example.sitiopro.shared.audit;

import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class AuditoriaConfigTests {

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void auditorSemAutenticacaoUsaSystem() {
        assertThat(new AuditoriaConfig().auditorAware().getCurrentAuditor())
                .contains("system");
    }

    @Test
    void auditorAutenticadoUsaLoginDoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Administrador");
        usuario.setLogin("admin");
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setSenhaHash("hash");
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                principal.getPassword(),
                principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(new AuditoriaConfig().auditorAware().getCurrentAuditor())
                .contains("admin");
    }
}
