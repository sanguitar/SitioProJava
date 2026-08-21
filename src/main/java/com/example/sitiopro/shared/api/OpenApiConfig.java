package com.example.sitiopro.shared.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sitioProOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sítio Guaratinguetá - API de Estoque")
                        .version("v1")
                        .description("Endpoints iniciais do módulo de estoque."));
    }
}
