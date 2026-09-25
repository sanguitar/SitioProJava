package com.example.sitiopro.criacao.suinos.web;

import com.example.sitiopro.criacao.suinos.dto.RegistroSanitarioSuinosRequest;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.service.*;
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
@RequestMapping("/sitio/criacoes/suinos/sanidade")
public class SuinosSanidadeController {
    private final SuinosSanidadeService sanidade;
    private final SuinosService suinos;
    private final SuinosReproducaoService reproducao;
    private final EstoqueCatalogoService estoque;
    private final Clock clock;

    public SuinosSanidadeController(SuinosSanidadeService sanidade, SuinosService suinos,
            SuinosReproducaoService reproducao, EstoqueCatalogoService estoque, Clock clock) {
        this.sanidade = sanidade;
        this.suinos = suinos;
        this.reproducao = reproducao;
        this.estoque = estoque;
        this.clock = clock;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long loteId,
            @RequestParam(required = false) Long animalId, Model model) {
        base(model);
        model.addAttribute("registros", sanidade.listar(loteId, animalId));
        model.addAttribute("resumo", sanidade.resumoOperacional());
        model.addAttribute("loteId", loteId);
        model.addAttribute("animalId", animalId);
        return "criacoes/suinos/sanidade/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long loteId,
            @RequestParam(required = false) Long animalId, Model model) {
        RegistroSanitarioSuinosRequest request = new RegistroSanitarioSuinosRequest();
        request.setLoteId(loteId);
        request.setAnimalReprodutivoId(animalId);
        request.setDataProcedimento(LocalDate.now(clock));
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        prepararForm(model, request);
        return "criacoes/suinos/sanidade/form";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroSanitarioSuinosRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) {
            prepararForm(model, request);
            return "criacoes/suinos/sanidade/form";
        }
        try {
            sanidade.registrar(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Registro sanitário incluído.");
            return redirecionar(request.getLoteId(), request.getAnimalReprodutivoId());
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("registroForm", ex.getMessage()));
            prepararForm(model, request);
            return "criacoes/suinos/sanidade/form";
        }
    }

    @PostMapping("/{id}/concluir-proxima-acao")
    public String concluirProximaAcao(@PathVariable Long id, RedirectAttributes redirect,
            Authentication authentication) {
        try {
            sanidade.concluirProximaAcao(id, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Próxima ação concluída.");
        } catch (RuntimeException ex) {
            redirect.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/criacoes/suinos/sanidade";
    }

    private void prepararForm(Model model, RegistroSanitarioSuinosRequest request) {
        base(model);
        model.addAttribute("registroForm", request);
        model.addAttribute("tipos", TipoRegistroSanitarioSuinos.values());
        model.addAttribute("lotes", suinos.listar(StatusLoteSuinos.ATIVO, null, 0, 100).conteudo());
        model.addAttribute("animais", reproducao.listarAnimais());
        model.addAttribute("itensEstoque", estoque.listarItensAtivos());
        model.addAttribute("locaisEstoque", estoque.listarLocaisAtivos());
    }

    private String redirecionar(Long loteId, Long animalId) {
        if (loteId != null) return "redirect:/sitio/criacoes/suinos/sanidade?loteId=" + loteId;
        if (animalId != null) return "redirect:/sitio/criacoes/suinos/sanidade?animalId=" + animalId;
        return "redirect:/sitio/criacoes/suinos/sanidade";
    }

    private void base(Model model) { model.addAttribute("active", "suinos"); }
}
