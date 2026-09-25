package com.example.sitiopro.criacao.suinos.web;

import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.service.*;
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
@RequestMapping("/sitio/criacoes/suinos/reproducao")
public class SuinosReproducaoController {
    private final SuinosReproducaoService reproducao;
    private final SuinosService suinos;
    private final InstalacaoCriacaoService instalacoes;
    private final Clock clock;

    public SuinosReproducaoController(SuinosReproducaoService reproducao, SuinosService suinos,
            InstalacaoCriacaoService instalacoes, Clock clock) {
        this.reproducao = reproducao; this.suinos = suinos; this.instalacoes = instalacoes; this.clock = clock;
    }

    @GetMapping
    public String inicio(Model model) {
        base(model); model.addAttribute("resumo", reproducao.resumoOperacional());
        model.addAttribute("animais", reproducao.listarAnimais()); model.addAttribute("ciclos", reproducao.listarCiclos());
        return "criacoes/suinos/reproducao/inicio";
    }

    @GetMapping("/animais/novo")
    public String novoAnimal(Model model) {
        CriarAnimalReprodutivoRequest request = new CriarAnimalReprodutivoRequest();
        request.setChaveIdempotencia(chave()); prepararAnimal(model, request);
        return "criacoes/suinos/reproducao/animal-form";
    }

    @PostMapping("/animais")
    public String criarAnimal(@Valid @ModelAttribute("animalForm") CriarAnimalReprodutivoRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) { prepararAnimal(model, request); return "criacoes/suinos/reproducao/animal-form"; }
        try { reproducao.cadastrarAnimal(request, UsuarioAtor.de(authentication)); redirect.addFlashAttribute("mensagem", "Animal reprodutivo identificado."); return "redirect:/sitio/criacoes/suinos/reproducao"; }
        catch (RuntimeException ex) { result.addError(new ObjectError("animalForm", ex.getMessage())); prepararAnimal(model, request); return "criacoes/suinos/reproducao/animal-form"; }
    }

    @GetMapping("/ciclos/novo")
    public String novoCiclo(Model model) {
        RegistrarCoberturaSuinosRequest request = new RegistrarCoberturaSuinosRequest();
        request.setDataCobertura(LocalDate.now(clock)); request.setMetodo(MetodoReproducaoSuinos.COBERTURA);
        request.setChaveIdempotencia(chave()); prepararCiclo(model, request);
        return "criacoes/suinos/reproducao/ciclo-form";
    }

    @PostMapping("/ciclos")
    public String criarCiclo(@Valid @ModelAttribute("cicloForm") RegistrarCoberturaSuinosRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) { prepararCiclo(model, request); return "criacoes/suinos/reproducao/ciclo-form"; }
        try { var ciclo = reproducao.registrarCobertura(request, UsuarioAtor.de(authentication)); redirect.addFlashAttribute("mensagem", "Cobertura registrada."); return redirecionar(ciclo.id()); }
        catch (RuntimeException ex) { result.addError(new ObjectError("cicloForm", ex.getMessage())); prepararCiclo(model, request); return "criacoes/suinos/reproducao/ciclo-form"; }
    }

    @GetMapping("/ciclos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        base(model); var ciclo = reproducao.detalhar(id); model.addAttribute("ciclo", ciclo);
        ChecagemGestacaoSuinosRequest checagem = new ChecagemGestacaoSuinosRequest(); checagem.setDataChecagem(LocalDate.now(clock));
        RegistrarPartoSuinosRequest parto = new RegistrarPartoSuinosRequest(); parto.setDataParto(LocalDate.now(clock)); parto.setNascidosVivos(0); parto.setNatimortos(0); parto.setPerdas(0); parto.setChaveIdempotencia(chave());
        RegistrarDesmameSuinosRequest desmame = new RegistrarDesmameSuinosRequest(); desmame.setDataDesmame(LocalDate.now(clock)); desmame.setChaveIdempotencia(chave());
        model.addAttribute("checagemForm", checagem); model.addAttribute("partoForm", parto); model.addAttribute("desmameForm", desmame);
        model.addAttribute("instalacoes", instalacoes.listarAtivas());
        return "criacoes/suinos/reproducao/detalhe";
    }

    @PostMapping("/ciclos/{id}/checagem")
    public String checagem(@PathVariable Long id, @Valid @ModelAttribute ChecagemGestacaoSuinosRequest request,
            BindingResult result, RedirectAttributes redirect, Authentication authentication) {
        return operar(id, result, redirect, () -> reproducao.registrarChecagem(id, request, UsuarioAtor.de(authentication)), "Checagem registrada.");
    }
    @PostMapping("/ciclos/{id}/parto")
    public String parto(@PathVariable Long id, @Valid @ModelAttribute RegistrarPartoSuinosRequest request,
            BindingResult result, RedirectAttributes redirect, Authentication authentication) {
        return operar(id, result, redirect, () -> reproducao.registrarParto(id, request, UsuarioAtor.de(authentication)), "Parto registrado e lote de leitões vinculado.");
    }
    @PostMapping("/ciclos/{id}/desmame")
    public String desmame(@PathVariable Long id, @Valid @ModelAttribute RegistrarDesmameSuinosRequest request,
            BindingResult result, RedirectAttributes redirect, Authentication authentication) {
        return operar(id, result, redirect, () -> reproducao.registrarDesmame(id, request, UsuarioAtor.de(authentication)), "Desmame registrado.");
    }

    private void prepararAnimal(Model model, CriarAnimalReprodutivoRequest request) {
        base(model); model.addAttribute("animalForm", request); model.addAttribute("tipos", TipoAnimalReprodutivo.values());
        model.addAttribute("lotes", suinos.listar(StatusLoteSuinos.ATIVO, null, 0, 100).conteudo().stream()
                .filter(l -> l.categoria() == CategoriaSuino.MATRIZ || l.categoria() == CategoriaSuino.REPRODUTOR).toList());
    }
    private void prepararCiclo(Model model, RegistrarCoberturaSuinosRequest request) {
        base(model); model.addAttribute("cicloForm", request); model.addAttribute("metodos", MetodoReproducaoSuinos.values());
        model.addAttribute("matrizes", reproducao.listarMatrizes()); model.addAttribute("reprodutores", reproducao.listarReprodutores());
    }
    private String operar(Long id, BindingResult result, RedirectAttributes redirect, Acao acao, String mensagem) {
        if (result.hasErrors()) { redirect.addFlashAttribute("erro", result.getAllErrors().getFirst().getDefaultMessage()); return redirecionar(id); }
        try { acao.executar(); redirect.addFlashAttribute("mensagem", mensagem); }
        catch (RuntimeException ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return redirecionar(id);
    }
    private void base(Model model) { model.addAttribute("active", "suinos"); }
    private String chave() { return UUID.randomUUID().toString(); }
    private String redirecionar(Long id) { return "redirect:/sitio/criacoes/suinos/reproducao/ciclos/" + id; }
    @FunctionalInterface private interface Acao { Object executar(); }
}
