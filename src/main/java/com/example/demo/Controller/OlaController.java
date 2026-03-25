package com.example.demo.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OlaController {

    @GetMapping("/ola")
    public String dizerOla() {
        return "Servidor Spring rodando na porta 8081 em Porto Velho!";
    }

    @GetMapping("/saudacao")
    public String saudacao(@RequestParam(value = "nome", defaultValue = "Porto Velho") String nome) {
        return "Olá, " + nome + "! O Spring Boot está rodando liso.";
    }
}