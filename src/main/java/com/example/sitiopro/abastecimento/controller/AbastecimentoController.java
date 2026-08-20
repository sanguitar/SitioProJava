package com.example.sitiopro.abastecimento.controller;

import com.example.sitiopro.abastecimento.model.Abastecimento;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.frota.service.VeiculoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/abastecimento")
public class AbastecimentoController {

    private final AbastecimentoService abastecimentoService;
    private final VeiculoService veiculoService;

    public AbastecimentoController(AbastecimentoService abastecimentoService, VeiculoService veiculoService) {
        this.abastecimentoService = abastecimentoService;
        this.veiculoService = veiculoService;
    }

    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("abastecimento", new Abastecimento());
        model.addAttribute("veiculos", veiculoService.listarTodos());
        return "abastecimento/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Abastecimento abastecimento, RedirectAttributes attributes) {
        try {
            abastecimentoService.registrarAbastecimento(abastecimento);
            attributes.addFlashAttribute("mensagem", "Abastecimento registrado com sucesso!");
            return "redirect:/sitio/frota";
        } catch (RuntimeException e) {
            attributes.addFlashAttribute("erro", "Erro ao registrar: " + e.getMessage());
            return "redirect:/sitio/abastecimento/novo";
        }
    }
}
