package com.example.sitiopro.tarefas.service;

import com.example.sitiopro.usuario.security.UsuarioPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

public record UsuarioAtor(Long id, String login, boolean admin) {

    public static UsuarioAtor de(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Autenticação obrigatória.");
        }
        Long id = authentication.getPrincipal() instanceof UsuarioPrincipal principal
                ? principal.getId()
                : null;
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        return new UsuarioAtor(id, authentication.getName(), admin);
    }

    public String ator() {
        return login == null || login.isBlank() ? "sistema" : login;
    }
}
