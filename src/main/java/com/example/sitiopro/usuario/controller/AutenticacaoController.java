package com.example.sitiopro.usuario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AutenticacaoController {

    @GetMapping("/login")
    public String login() {
        return "security/login";
    }

    @GetMapping("/403")
    public String acessoNegado() {
        return "security/403";
    }
}
