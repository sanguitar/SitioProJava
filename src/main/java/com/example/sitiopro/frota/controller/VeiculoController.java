package com.example.sitiopro.frota.controller;

import com.example.sitiopro.frota.model.Veiculo;
import com.example.sitiopro.frota.service.VeiculoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/frota")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("veiculos", veiculoService.listarTodos());
        model.addAttribute("usuario", "Systems Analyst");
        return "frota/lista";
    }

    @GetMapping("/novo")
    public String exibirFormulario(Model model) {
        model.addAttribute("veiculo", new Veiculo());
        return "frota/cadastro";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Veiculo veiculo) {
        veiculoService.salvar(veiculo);
        return "redirect:/sitio/frota";
    }
}
