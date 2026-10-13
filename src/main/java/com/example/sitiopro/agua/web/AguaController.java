package com.example.sitiopro.agua.web;

import com.example.sitiopro.agua.dto.*;
import com.example.sitiopro.agua.entity.TipoOperacaoAgua;
import com.example.sitiopro.agua.service.AguaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import java.time.*;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/agua")
public class AguaController {
    private final AguaService service;
    private final Clock clock;

    public AguaController(AguaService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @InitBinder("operacaoForm")
    void bindOperacao(WebDataBinder binder) {
        binder.setAllowedFields("recursoOrigemId", "recursoDestinoId", "tipo", "dataOperacao",
                "quantidadeLitros", "observacao", "chaveIdempotencia");
    }

    @InitBinder("configForm")
    void bindConfig(WebDataBinder binder) {
        binder.setAllowedFields("nivelMinimoAlertaLitros", "intervaloInspecaoDias", "proximaInspecao", "versao");
    }

    @GetMapping
    public String dashboard(Model model) {
        base(model); model.addAttribute("resumo", service.dashboard());
        return "agua/dashboard";
    }

    @GetMapping({"/reservatorios", "/recursos"})
    public String recursos(Model model) {
        base(model); model.addAttribute("recursos", service.listarRecursos());
        return "agua/recursos/lista";
    }

    @GetMapping("/recursos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        base(model); model.addAttribute("recurso", service.detalharRecurso(id));
        model.addAttribute("operacoes", service.listarOperacoesDoRecurso(id));
        return "agua/recursos/detalhe";
    }

    @GetMapping("/recursos/{id}/configuracao")
    public String configuracao(@PathVariable Long id, Model model) {
        prepararConfiguracao(model, id, service.formularioConfiguracao(id));
        return "agua/recursos/configuracao";
    }

    @PostMapping("/recursos/{id}/configuracao")
    public String configurar(@PathVariable Long id,
            @Valid @ModelAttribute("configForm") RecursoHidricoOperacaoConfigRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) {
            prepararConfiguracao(model, id, request); return "agua/recursos/configuracao";
        }
        try {
            service.configurar(id, request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Configuração operacional atualizada.");
            return "redirect:/sitio/agua/recursos/" + id;
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("configForm", ex.getMessage()));
            prepararConfiguracao(model, id, request); return "agua/recursos/configuracao";
        }
    }

    @GetMapping("/registros")
    public String operacoes(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model); model.addAttribute("pagina", service.listarOperacoes(pagina, 30));
        return "agua/operacoes/lista";
    }

    @GetMapping("/registros/novo")
    public String novaOperacao(@RequestParam(required = false) Long recursoId, Model model) {
        OperacaoAguaRequest request = new OperacaoAguaRequest();
        request.setRecursoOrigemId(recursoId);
        request.setDataOperacao(LocalDateTime.now(clock).withSecond(0).withNano(0));
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        prepararOperacao(model, request);
        return "agua/operacoes/form";
    }

    @PostMapping("/registros")
    public String registrar(@Valid @ModelAttribute("operacaoForm") OperacaoAguaRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) {
            prepararOperacao(model, request); return "agua/operacoes/form";
        }
        try {
            var criada = service.registrar(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Operação hídrica registrada.");
            return "redirect:/sitio/agua/recursos/" + criada.recursoOrigemId();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("operacaoForm", ex.getMessage()));
            prepararOperacao(model, request); return "agua/operacoes/form";
        }
    }

    @GetMapping({"/bombas", "/manutencoes"})
    public String manutencoes(Model model) {
        base(model); model.addAttribute("resumo", service.dashboard());
        return "agua/manutencoes";
    }

    private void prepararOperacao(Model model, OperacaoAguaRequest request) {
        base(model); model.addAttribute("operacaoForm", request);
        model.addAttribute("tiposOperacao", TipoOperacaoAgua.values());
        model.addAttribute("recursos", service.listarRecursos().stream().filter(r -> r.ativo()).toList());
    }

    private void prepararConfiguracao(Model model, Long id, RecursoHidricoOperacaoConfigRequest request) {
        base(model); model.addAttribute("recurso", service.detalharRecurso(id));
        model.addAttribute("configForm", request);
    }

    private void base(Model model) { model.addAttribute("active", "agua"); }
}
