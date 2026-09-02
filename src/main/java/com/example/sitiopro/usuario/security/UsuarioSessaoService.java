package com.example.sitiopro.usuario.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class UsuarioSessaoService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioSessaoService.class);

    private final SessionRegistry sessionRegistry;

    public UsuarioSessaoService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public void revogarAposCommit(Long usuarioId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    revogarAgora(usuarioId);
                }
            });
            return;
        }
        revogarAgora(usuarioId);
    }

    public int revogarAgora(Long usuarioId) {
        int revogadas = 0;
        for (Object principal : List.copyOf(sessionRegistry.getAllPrincipals())) {
            if (!(principal instanceof UsuarioPrincipal usuarioPrincipal)
                    || !usuarioId.equals(usuarioPrincipal.getId())) {
                continue;
            }
            for (SessionInformation sessao : sessionRegistry.getAllSessions(principal, false)) {
                sessao.expireNow();
                revogadas++;
            }
        }
        log.info("Sessões revogadas após desativação de usuário: {}.", revogadas);
        return revogadas;
    }
}
