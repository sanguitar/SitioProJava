package com.example.sitiopro.usuario.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AutenticacaoController {

    @GetMapping("/login")
    public String login() {
        return "security/login";
    }

    // AccessDeniedHandler forwards the original HTTP method to this view.
    @RequestMapping("/403")
    public String acessoNegado() {
        return "security/403";
    }
}
