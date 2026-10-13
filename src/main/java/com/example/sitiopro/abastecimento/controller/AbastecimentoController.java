package com.example.sitiopro.abastecimento.controller;

import com.example.sitiopro.abastecimento.dto.AbastecimentoRequest;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.frota.service.VeiculoService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.*;
import java.util.UUID;

@Controller
public class AbastecimentoController {
    private final AbastecimentoService abastecimentos;
    private final VeiculoService veiculos;
    private final Clock clock;

    public AbastecimentoController(AbastecimentoService abastecimentos, VeiculoService veiculos, Clock clock) {
        this.abastecimentos = abastecimentos; this.veiculos = veiculos; this.clock = clock;
    }

    @InitBinder("abastecimentoForm")
    void restringirCampos(WebDataBinder binder) {
        binder.setAllowedFields("veiculoId", "data", "litros", "precoPorLitro", "kmNoAto",
                "horimetroNoAto", "local", "chaveIdempotencia");
    }

    @GetMapping({"/sitio/abastecimento/novo", "/sitio/abastecimentos/novo"})
    public String novo(@RequestParam(required = false) Long veiculoId, Model model) {
        AbastecimentoRequest request = new AbastecimentoRequest();
        request.setVeiculoId(veiculoId); request.setData(LocalDate.now(clock));
        request.setChaveIdempotencia(UUID.randomUUID().toString());
        prepararFormulario(model, request); return "abastecimento/form";
    }

    @PostMapping({"/sitio/abastecimento/salvar", "/sitio/abastecimentos"})
    public String salvar(@Valid @ModelAttribute("abastecimentoForm") AbastecimentoRequest request,
            BindingResult result, Model model, RedirectAttributes redirect, Authentication authentication) {
        if (result.hasErrors()) { prepararFormulario(model, request); return "abastecimento/form"; }
        try {
            var criado = abastecimentos.registrar(request, UsuarioAtor.de(authentication));
            redirect.addFlashAttribute("mensagem", "Abastecimento registrado com sucesso.");
            return "redirect:/sitio/abastecimentos/" + criado.id();
        } catch (RuntimeException ex) {
            result.addError(new ObjectError("abastecimentoForm", ex.getMessage()));
            prepararFormulario(model, request); return "abastecimento/form";
        }
    }

    @GetMapping({"/sitio/abastecimentos", "/sitio/abastecimentos/historico", "/sitio/abastecimentos/detalhe"})
    public String listar(@RequestParam(required = false) Long veiculoId,
            @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", abastecimentos.listar(veiculoId, pagina, 20));
        model.addAttribute("veiculos", veiculos.listarTodos()); model.addAttribute("veiculoId", veiculoId);
        return "abastecimento/lista";
    }

    @GetMapping("/sitio/abastecimentos/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("abastecimento", abastecimentos.detalhar(id));
        return "abastecimento/detalhe";
    }

    private void prepararFormulario(Model model, AbastecimentoRequest request) {
        model.addAttribute("abastecimentoForm", request); model.addAttribute("veiculos", veiculos.listarTodos());
    }
}
