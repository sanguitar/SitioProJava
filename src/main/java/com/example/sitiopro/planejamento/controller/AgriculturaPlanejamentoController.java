package com.example.sitiopro.planejamento.controller;

import com.example.sitiopro.planejamento.PlanejamentoView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/agricultura")
public class AgriculturaPlanejamentoController {

    @GetMapping({
            "/areas",
            "/areas/{acao}",
            "/culturas",
            "/culturas/{acao}",
            "/plantios",
            "/plantios/{acao}",
            "/adubacao",
            "/adubacao/{acao}",
            "/irrigacao",
            "/irrigacao/{acao}",
            "/tratamentos",
            "/tratamentos/{acao}",
            "/colheitas",
            "/colheitas/{acao}"
    })
    public String pagina(HttpServletRequest request, Model model) {
        return PlanejamentoView.renderizar(request, model);
    }
}
