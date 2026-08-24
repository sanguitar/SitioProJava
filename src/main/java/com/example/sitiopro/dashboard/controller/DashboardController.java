package com.example.sitiopro.dashboard.controller;

import com.example.sitiopro.dashboard.dto.DashboardOperacionalResumo;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.planejamento.PlanejamentoCatalogo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/painel")
    public String exibirPainel(Model model) {
        DashboardOperacionalResumo resumo = dashboardService.montarResumo();
        model.addAttribute("resumo", resumo);
        model.addAttribute("usuario", PlanejamentoCatalogo.USUARIO_VISUAL);
        return "dashboard/painel";
    }
}
