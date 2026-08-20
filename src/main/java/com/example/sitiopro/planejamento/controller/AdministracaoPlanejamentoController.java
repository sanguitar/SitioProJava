package com.example.sitiopro.planejamento.controller;

import com.example.sitiopro.planejamento.PlanejamentoCatalogo;
import com.example.sitiopro.planejamento.PlanejamentoView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/admin")
public class AdministracaoPlanejamentoController {

    @GetMapping("/roadmap")
    public String roadmap(Model model) {
        model.addAttribute("usuario", PlanejamentoCatalogo.USUARIO_VISUAL);
        model.addAttribute("active", "roadmap");
        model.addAttribute("roadmapGrupos", PlanejamentoCatalogo.roadmapPorGrupo());
        model.addAttribute("statusDisponiveis", PlanejamentoCatalogo.statusDisponiveis());
        return "admin/roadmap";
    }

    @GetMapping({
            "/configuracoes",
            "/configuracoes/{acao}",
            "/centros-custo",
            "/centros-custo/{acao}",
            "/unidades-medida",
            "/unidades-medida/{acao}",
            "/propriedade",
            "/propriedade/{acao}"
    })
    public String pagina(HttpServletRequest request, Model model) {
        return PlanejamentoView.renderizar(request, model);
    }
}
