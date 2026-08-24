package com.example.sitiopro.criacao.aves.web;

import com.example.sitiopro.criacao.aves.service.AvesOperacaoException;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoRequest;
import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/criacoes/aves/instalacoes")
public class InstalacoesAvesController {
    private final InstalacaoCriacaoService service;
    public InstalacoesAvesController(InstalacaoCriacaoService service) { this.service = service; }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int pagina, Model model) {
        base(model); model.addAttribute("pagina", service.listar(pagina, 20)); return "criacoes/aves/instalacoes/lista";
    }

    @GetMapping("/nova") public String nova(Model model) { formulario(model, new InstalacaoCriacaoRequest(), null); return "criacoes/aves/instalacoes/form"; }

    @PostMapping
    public String criar(@Valid @ModelAttribute("instalacaoForm") InstalacaoCriacaoRequest request,
            BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { formulario(model, request, null); return "criacoes/aves/instalacoes/form"; }
        try { var criada = service.criar(request); redirect.addFlashAttribute("mensagem", "Instalação cadastrada."); return "redirect:/sitio/criacoes/aves/instalacoes/" + criada.id(); }
        catch (AvesOperacaoException ex) { result.addError(new ObjectError("instalacaoForm", ex.getMessage())); formulario(model, request, null); return "criacoes/aves/instalacoes/form"; }
    }

    @GetMapping("/{id}") public String detalhe(@PathVariable Long id, Model model) { base(model); model.addAttribute("instalacao", service.detalhar(id)); return "criacoes/aves/instalacoes/detalhe"; }
    @GetMapping("/{id}/editar") public String editar(@PathVariable Long id, Model model) { formulario(model, service.formulario(id), id); return "criacoes/aves/instalacoes/form"; }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("instalacaoForm") InstalacaoCriacaoRequest request,
            BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) { formulario(model, request, id); return "criacoes/aves/instalacoes/form"; }
        try { service.atualizar(id, request); redirect.addFlashAttribute("mensagem", "Instalação atualizada."); }
        catch (AvesOperacaoException ex) { redirect.addFlashAttribute("erro", ex.getMessage()); }
        return "redirect:/sitio/criacoes/aves/instalacoes/" + id;
    }

    private void base(Model m) { m.addAttribute("active", "aves"); }
    private void formulario(Model m, InstalacaoCriacaoRequest r, Long id) { base(m); m.addAttribute("instalacaoForm", r); m.addAttribute("instalacaoId", id); m.addAttribute("tipos", TipoInstalacaoCriacao.values()); }
}
