package com.example.sitiopro.planejamento.controller;

import com.example.sitiopro.planejamento.PlanejamentoView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio")
public class PropriedadePlanejamentoController {

    @GetMapping({
            "/casa",
            "/casa/{acao}",
            "/despensa",
            "/despensa/{acao}",
            "/manutencao",
            "/manutencao/{acao}",
            "/ar-condicionado",
            "/ar-condicionado/{acao}",
            "/dedetizacao",
            "/dedetizacao/{acao}",
            "/reformas",
            "/reformas/{acao}",
            "/deterioracoes",
            "/deterioracoes/{acao}",
            "/patrimonio",
            "/patrimonio/{acao}",
            "/seguranca",
            "/seguranca/{acao}"
    })
    public String pagina(HttpServletRequest request, Model model) {
        return PlanejamentoView.renderizar(request, model);
    }
}
