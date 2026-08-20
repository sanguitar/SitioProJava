package com.example.sitiopro.dashboard.controller;

import com.example.sitiopro.dashboard.dto.DashboardResumo;
import com.example.sitiopro.dashboard.service.DashboardService;
import com.example.sitiopro.planejamento.PlanejamentoCatalogo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/sitio")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/painel")
    public String exibirPainel(
            @RequestParam(value = "categoriaId", required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        DashboardResumo resumo = dashboardService.montarResumo(categoriaId, page);

        model.addAttribute("itens", resumo.paginaEstoque().getContent());
        model.addAttribute("paginaAtual", Math.max(page, 0));
        model.addAttribute("totalPaginas", resumo.paginaEstoque().getTotalPages());
        model.addAttribute("totalItens", resumo.totalItens());
        model.addAttribute("totalCategorias", resumo.totalCategorias());
        model.addAttribute("categoriaSelecionada", categoriaId);
        model.addAttribute("categorias", resumo.categorias());
        model.addAttribute("labelsGraficoJson", resumo.labelsGraficoJson());
        model.addAttribute("dadosGraficoJson", resumo.dadosGraficoJson());
        model.addAttribute("itensAlerta", resumo.itensAlerta());
        model.addAttribute("usuario", PlanejamentoCatalogo.USUARIO_VISUAL);

        return "dashboard/painel";
    }
}
