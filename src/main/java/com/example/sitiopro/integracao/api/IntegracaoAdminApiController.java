package com.example.sitiopro.integracao.api;

import com.example.sitiopro.integracao.core.dto.IntegracaoPainelResumo;
import com.example.sitiopro.integracao.core.service.IntegracaoPainelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/integracoes")
@Tag(name = "Integrações", description = "Estado operacional persistido das integrações externas")
public class IntegracaoAdminApiController {

    private final IntegracaoPainelService painelService;

    public IntegracaoAdminApiController(IntegracaoPainelService painelService) {
        this.painelService = painelService;
    }

    @GetMapping
    @Operation(summary = "Consulta o estado das integrações externas")
    public IntegracaoPainelResumo listar() {
        return painelService.resumo();
    }
}
