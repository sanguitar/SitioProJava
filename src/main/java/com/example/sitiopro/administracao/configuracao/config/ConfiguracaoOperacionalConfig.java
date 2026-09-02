package com.example.sitiopro.administracao.configuracao.config;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ConfiguracaoOperacionalInicialProperties.class)
public class ConfiguracaoOperacionalConfig {
    @Bean
    ApplicationRunner inicializarConfiguracaoOperacional(ConfiguracaoOperacionalService service) {
        return args -> service.inicializar();
    }
}
