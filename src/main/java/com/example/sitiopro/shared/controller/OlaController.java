package com.example.sitiopro.shared.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OlaController {

    @GetMapping("/ola")
    public String dizerOla() {
        return "Servidor Spring rodando no Sítio Guaratinguetá.";
    }

    @GetMapping("/saudacao")
    public String saudacao(@RequestParam(value = "nome", defaultValue = "Guaratinguetá") String nome) {
        return "Olá, " + nome + "! O Spring Boot está rodando liso.";
    }
}
