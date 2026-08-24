package com.example.sitiopro.dashboard.api;

import com.example.sitiopro.dashboard.dto.DashboardOperacionalResumo;
import com.example.sitiopro.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/painel")
@Tag(name = "Painel", description = "Resumo operacional do Sítio Guaratinguetá")
public class DashboardApiController {

    private final DashboardService dashboardService;

    public DashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resume as prioridades operacionais da propriedade")
    public DashboardOperacionalResumo resumo() {
        return dashboardService.montarResumo();
    }
}
