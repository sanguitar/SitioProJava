package com.example.sitiopro.usuario.security;

import com.example.sitiopro.shared.observability.MdcScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);

    @EventListener
    public void loginSucesso(AuthenticationSuccessEvent event) {
        String userName = event.getAuthentication() == null ? "unknown" : event.getAuthentication().getName();
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "LOGIN_SUCCESS",
                "module", "usuario",
                "user.name", userName))) {
            log.info("Login realizado com sucesso.");
        }
    }

    @EventListener
    public void loginFalhou(AbstractAuthenticationFailureEvent event) {
        String userName = event.getAuthentication() == null ? "unknown" : String.valueOf(event.getAuthentication().getPrincipal());
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "LOGIN_FAILURE",
                "module", "usuario",
                "user.name", sanitizeUserName(userName)))) {
            log.warn("Falha de login.");
        }
    }

    @EventListener
    public void logout(LogoutSuccessEvent event) {
        String userName = event.getAuthentication() == null ? "unknown" : event.getAuthentication().getName();
        try (MdcScope ignored = MdcScope.with(Map.of(
                "event.action", "LOGOUT",
                "module", "usuario",
                "user.name", userName))) {
            log.info("Logout realizado.");
        }
    }

    private String sanitizeUserName(String userName) {
        if (userName == null || userName.isBlank()) {
            return "unknown";
        }
        return userName.length() > 80 ? userName.substring(0, 80) : userName;
    }
}
