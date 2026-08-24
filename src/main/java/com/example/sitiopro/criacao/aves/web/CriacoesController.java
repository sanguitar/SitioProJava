package com.example.sitiopro.criacao.aves.web;

import com.example.sitiopro.criacao.aves.service.AvesResumoService;
import com.example.sitiopro.criacao.aves.service.IncubacaoAvesService;
import com.example.sitiopro.criacao.aves.service.LoteAvesService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio")
public class CriacoesController {
    private final AvesResumoService resumoService;
    private final LoteAvesService loteService;
    private final IncubacaoAvesService incubacaoService;

    public CriacoesController(AvesResumoService resumoService, LoteAvesService loteService,
            IncubacaoAvesService incubacaoService) {
        this.resumoService = resumoService; this.loteService = loteService; this.incubacaoService = incubacaoService;
    }

    @GetMapping("/criacoes")
    public String criacoes(Model model) { model.addAttribute("active", "aves"); return "criacoes/index"; }

    @GetMapping({"/criacoes/aves", "/aves"})
    public String aves(Model model) {
        model.addAttribute("active", "aves"); model.addAttribute("resumo", resumoService.resumo());
        model.addAttribute("lotes", loteService.listar(null, null, 0, 5).conteudo());
        model.addAttribute("incubacoes", incubacaoService.listar(0, 5).conteudo());
        return "criacoes/aves/dashboard";
    }

    @GetMapping("/aves/chocadeira") public String chocadeira() { return "redirect:/sitio/criacoes/aves/incubacoes"; }
    @GetMapping("/aves/pinteiro") public String pinteiro() { return "redirect:/sitio/criacoes/aves/instalacoes?tipo=CRIADOURO_PINTINHOS"; }
    @GetMapping("/aves/galinheiro") public String galinheiro() { return "redirect:/sitio/criacoes/aves/instalacoes?tipo=GALINHEIRO"; }
}
