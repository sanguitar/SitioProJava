package com.example.demo.Controller;

import com.example.demo.Model.Abastecimento;
import com.example.demo.Service.AbastecimentoService;
import com.example.demo.Service.VeiculoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/abastecimento")
public class AbastecimentoController {

    @Autowired
    private AbastecimentoService abastecimentoService; // CORRIGIDO: Tipo Service agora

    @Autowired
    private VeiculoService veiculoService;

    @GetMapping("/novo")
    public String formulario(Model model) {
        model.addAttribute("abastecimento", new Abastecimento());
        model.addAttribute("veiculos", veiculoService.listarTodos());
        return "frota/abastecimento-form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Abastecimento abastecimento, RedirectAttributes attributes) {
        try {
            abastecimentoService.registrarAbastecimento(abastecimento);
            attributes.addFlashAttribute("mensagem", "Abastecimento registrado com sucesso!");
            return "redirect:/sitio/frota";
        } catch (Exception e) {
            // Se der erro (ex: veículo não encontrado), volta com a mensagem de erro
            attributes.addFlashAttribute("erro", "Erro ao registrar: " + e.getMessage());
            return "redirect:/sitio/abastecimento/novo";
        }
    }
}