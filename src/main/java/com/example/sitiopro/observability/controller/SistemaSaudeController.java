package com.example.sitiopro.observability.controller;

import com.example.sitiopro.observability.service.SistemaSaudeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sitio/admin/saude")
public class SistemaSaudeController {

    private final SistemaSaudeService sistemaSaudeService;

    public SistemaSaudeController(SistemaSaudeService sistemaSaudeService) {
        this.sistemaSaudeService = sistemaSaudeService;
    }

    @GetMapping
    public String saude(Model model) {
        model.addAttribute("active", "saude");
        model.addAttribute("resumo", sistemaSaudeService.resumo());
        return "admin/saude";
    }
}
