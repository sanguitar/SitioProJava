package com.example.sitiopro.planejamento.controller;

import com.example.sitiopro.planejamento.PlanejamentoView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/agua")
public class AguaPlanejamentoController {

    @GetMapping({
            "/novo",
            "/detalhe",
            "/historico",
            "/reservatorios/novo",
            "/reservatorios/detalhe",
            "/reservatorios/historico",
            "/bombas/novo",
            "/bombas/detalhe",
            "/bombas/historico",
            "/irrigacao",
            "/irrigacao/novo",
            "/irrigacao/detalhe",
            "/irrigacao/historico",
            "/registros/detalhe",
            "/registros/historico",
            "/manutencoes/novo",
            "/manutencoes/detalhe",
            "/manutencoes/historico"
    })
    public String pagina(HttpServletRequest request, Model model) {
        return PlanejamentoView.renderizar(request, model);
    }
}
