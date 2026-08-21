package com.example.sitiopro.usuario.config;

import com.example.sitiopro.usuario.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@EnableConfigurationProperties(InitialAdminProperties.class)
public class InitialAdminBootstrap implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialAdminBootstrap.class);

    private final InitialAdminProperties properties;
    private final UsuarioService usuarioService;

    public InitialAdminBootstrap(InitialAdminProperties properties, UsuarioService usuarioService) {
        this.properties = properties;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (!StringUtils.hasText(properties.getLogin()) || !StringUtils.hasText(properties.getName())) {
            throw new IllegalStateException("Bootstrap de ADMIN inicial habilitado sem login ou nome.");
        }
        if (usuarioService.existePorLogin(properties.getLogin())) {
            LOGGER.info("Bootstrap de ADMIN inicial ignorado: usuário inicial já existe.");
            return;
        }
        if (usuarioService.contarUsuarios() > 0) {
            LOGGER.info("Bootstrap de ADMIN inicial ignorado: tabela de usuários já possui registros.");
            return;
        }
        if (!StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException("Bootstrap de ADMIN inicial habilitado sem senha.");
        }

        usuarioService.criarAdminInicial(properties.getName(), properties.getLogin(), properties.getPassword());
        LOGGER.info("ADMIN inicial criado por bootstrap. Desabilite SITIOPRO_INITIAL_ADMIN_ENABLED após o primeiro acesso.");
    }
}
