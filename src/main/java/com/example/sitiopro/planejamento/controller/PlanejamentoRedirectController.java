package com.example.sitiopro.planejamento.controller;

import com.example.sitiopro.planejamento.PlanejamentoCatalogo;
import com.example.sitiopro.planejamento.PlanejamentoView;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class PlanejamentoRedirectController {

    @GetMapping({
            "/configuracoes/roadmap",
            "/gestao/{modulo}",
            "/gestao/{modulo}/{acao}",
            "/criacoes/{modulo}",
            "/criacoes/{modulo}/{segmento}",
            "/criacoes/{modulo}/{segmento}/{acao}",
            "/agricultura/{modulo}",
            "/agricultura/{modulo}/{acao}",
            "/agua/{modulo}",
            "/agua/{modulo}/{acao}",
            "/propriedade/{modulo}",
            "/propriedade/{modulo}/{acao}",
            "/administracao/{modulo}",
            "/administracao/{modulo}/{acao}",
            "/sitio/abastecimento",
            "/sitio/abastecimento/detalhe",
            "/sitio/abastecimento/historico"
    })
    public String redirecionar(HttpServletRequest request) {
        String rota = PlanejamentoView.normalizarRota(request);
        String destino = PlanejamentoCatalogo.buscarRedirecionamento(rota);

        if (destino == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return "redirect:" + destino;
    }
}
