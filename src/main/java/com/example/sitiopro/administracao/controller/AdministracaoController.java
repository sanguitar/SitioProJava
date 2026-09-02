package com.example.sitiopro.administracao.controller;

import com.example.sitiopro.administracao.service.AdministracaoResumoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/admin")
public class AdministracaoController {

    private final AdministracaoResumoService resumoService;

    public AdministracaoController(AdministracaoResumoService resumoService) {
        this.resumoService = resumoService;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("active", "admin");
        model.addAttribute("resumo", resumoService.resumo());
        return "admin/index";
    }
}
