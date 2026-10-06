package com.example.sitiopro.criacao.peixes.web;

import com.example.sitiopro.criacao.peixes.dto.RegistroSanitarioPeixesRequest;
import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.service.*;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.*;
import java.util.UUID;

@Controller
@RequestMapping("/sitio/criacoes/peixes/sanidade")
public class PeixesSanidadeController {
    private final PeixesSanidadeService sanidade;
    private final PeixesService peixes;
    private final EstoqueCatalogoService estoque;
    private final Clock clock;

    public PeixesSanidadeController(PeixesSanidadeService sanidade, PeixesService peixes,
            EstoqueCatalogoService estoque, Clock clock) {
        this.sanidade = sanidade;
        this.peixes = peixes;
        this.estoque = estoque;
        this.clock = clock;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long loteId, Model model) {
        base(model);
        model.addAttribute("registros", sanidade.listar(loteId));
        model.addAttribute("resumo", sanidade.resumoOperacional());
        model.addAttribute("loteId", loteId);
        return "criacoes/peixes/sanidade/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long loteId, Model model) {
        RegistroSanitarioPeixesRequest request = new RegistroSanitarioPeixesRequest();
        request.setLoteId(loteId);
        request.setDataProcedimento(LocalDateTime.now(clock).withSecond(0).withNano(0));
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        prepararForm(model, request);
        return "criacoes/peixes/sanidade/form";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroSanitarioPeixesRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) {
            prepararForm(model, request);
            return "criacoes/peixes/sanidade/form";
        }
        try {
            sanidade.registrar(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Registro sanitário incluído.");
            return redirecionar(request.getLoteId());
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("registroForm", ex.getMessage()));
            prepararForm(model, request);
            return "criacoes/peixes/sanidade/form";
        }
    }

    @PostMapping("/{id}/concluir-proxima-acao")
    public String concluirProximaAcao(@PathVariable Long id, RedirectAttributes redirect,
            Authentication authentication) {
        try {
            var registro = sanidade.concluirProximaAcao(id, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Próxima ação concluída.");
            return redirecionar(registro.loteId());
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/sitio/criacoes/peixes/sanidade";
        }
    }

    private void prepararForm(Model model, RegistroSanitarioPeixesRequest request) {
        base(model);
        model.addAttribute("registroForm", request);
        model.addAttribute("tipos", TipoRegistroSanitarioPeixes.values());
        model.addAttribute("lotes", peixes.listar(StatusLotePeixes.ATIVO, null, 0, 100).conteudo());
        model.addAttribute("itensEstoque", estoque.listarItensAtivos());
        model.addAttribute("locaisEstoque", estoque.listarLocaisAtivos());
    }

    private String redirecionar(Long loteId) {
        return loteId == null ? "redirect:/sitio/criacoes/peixes/sanidade"
                : "redirect:/sitio/criacoes/peixes/sanidade?loteId=" + loteId;
    }
    private void base(Model model) { model.addAttribute("active", "piscicultura"); }
}
