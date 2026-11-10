package com.example.sitiopro.sync;

import com.example.sitiopro.shared.api.ApiErrorResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
@Tag(name = "Sincronização offline", description = "Piloto offline-first para Tarefas e medições de Água")
@SecurityRequirement(name = "bearerAuth")
public class SyncController {
    private final SyncService service;

    public SyncController(SyncService service) {
        this.service = service;
    }

    @GetMapping("/bootstrap")
    @Operation(summary = "Obtém o snapshot inicial e um cursor monotônico válido")
    public SyncDtos.BootstrapResponse bootstrap() {
        return service.bootstrap();
    }

    @PostMapping
    @Operation(summary = "Envia comandos offline e recebe mudanças posteriores ao cursor")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comandos processados; conflitos ficam no corpo"),
            @ApiResponse(responseCode = "400", description = "Contrato ou payload inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Bearer inválido",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "operationId reutilizada com outro payload",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public SyncDtos.SyncResponse sincronizar(@Valid @RequestBody SyncDtos.SyncRequest request,
            Authentication authentication) {
        return service.sincronizar(request, UsuarioAtor.de(authentication));
    }
}
