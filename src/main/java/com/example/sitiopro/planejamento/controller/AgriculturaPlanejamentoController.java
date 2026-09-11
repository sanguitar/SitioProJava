package com.example.sitiopro.planejamento.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/agricultura")
public class AgriculturaPlanejamentoController {

    @GetMapping({"/areas", "/areas/{acao}"})
    public String talhoes() { return "redirect:/sitio/propriedade/talhoes"; }

    @GetMapping({"/plantios", "/plantios/{acao}"})
    public String plantios() { return "redirect:/sitio/agricultura/cultivos"; }

    @GetMapping({"/culturas/cadastro", "/culturas/detalhe", "/culturas/historico"})
    public String culturas() { return "redirect:/sitio/agricultura/culturas"; }

    @GetMapping({"/colheitas/novo", "/colheitas/cadastro", "/colheitas/detalhe", "/colheitas/historico"})
    public String colheitas() { return "redirect:/sitio/agricultura/colheitas"; }
}
