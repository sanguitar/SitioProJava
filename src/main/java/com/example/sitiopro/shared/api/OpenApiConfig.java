package com.example.sitiopro.shared.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sitioProOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sítio Guaratinguetá - API operacional")
                        .version("v1")
                        .description("Contrato oficial da API v1 para clientes web integrados, iOS e Android. "
                                + "Access tokens expiram em 15 minutos por padrão; refresh tokens rotativos, "
                                + "revogáveis e armazenados somente como hash expiram em 30 dias por padrão."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Access token retornado por POST /api/v1/auth/login ou /refresh.")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    @Bean
    OpenApiCustomizer respostasRestPadronizadas() {
        return openApi -> openApi.getPaths().values().forEach(path -> path.readOperationsMap()
                .forEach((method, operation) -> {
                    boolean publica = operation.getSecurity() != null && operation.getSecurity().isEmpty();
                    if (!publica) {
                        adicionarSeAusente(operation.getResponses(), "401", "Access token ausente, inválido, expirado ou revogado");
                        adicionarSeAusente(operation.getResponses(), "403", "Usuário autenticado sem permissão para a operação");
                    }
                    if (List.of(io.swagger.v3.oas.models.PathItem.HttpMethod.POST,
                            io.swagger.v3.oas.models.PathItem.HttpMethod.PUT,
                            io.swagger.v3.oas.models.PathItem.HttpMethod.PATCH).contains(method)) {
                        adicionarSeAusente(operation.getResponses(), "400", "Payload ou parâmetros inválidos");
                    }
                    adicionarSeAusente(operation.getResponses(), "500", "Erro interno com requestId para correlação");
                }));
    }

    private void adicionarSeAusente(io.swagger.v3.oas.models.responses.ApiResponses respostas,
            String codigo, String descricao) {
        if (!respostas.containsKey(codigo)) {
            respostas.addApiResponse(codigo, new ApiResponse()
                    .description(descricao)
                    .content(new Content().addMediaType("application/json", new MediaType()
                            .schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse")))));
        }
    }
}
