package com.example.sitiopro.planejamento.controller;

import com.example.sitiopro.planejamento.PlanejamentoView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio")
public class VeiculosPlanejamentoController {

    @GetMapping({
            "/frota/detalhe",
            "/frota/historico",
            "/abastecimentos",
            "/abastecimentos/detalhe",
            "/abastecimentos/historico"
    })
    public String pagina(HttpServletRequest request, Model model) {
        return PlanejamentoView.renderizar(request, model);
    }

    @GetMapping("/abastecimentos/novo")
    public String novoAbastecimento() {
        return "redirect:/sitio/abastecimento/novo";
    }
}
