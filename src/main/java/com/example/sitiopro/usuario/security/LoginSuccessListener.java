package com.example.sitiopro.usuario.security;

import com.example.sitiopro.usuario.service.UsuarioService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessListener {

    private final UsuarioService usuarioService;

    public LoginSuccessListener(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @EventListener
    public void registrarLogin(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof UsuarioPrincipal usuario) {
            usuarioService.registrarUltimoLogin(usuario.getId());
        }
    }
}
