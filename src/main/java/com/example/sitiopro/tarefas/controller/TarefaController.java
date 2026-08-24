package com.example.sitiopro.tarefas.controller;

import com.example.sitiopro.tarefas.dto.PrazoTarefa;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.dto.TarefaFiltro;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.TipoRecorrencia;
import com.example.sitiopro.tarefas.service.ResumoOperacionalService;
import com.example.sitiopro.tarefas.service.TarefaAlertaOperacaoException;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;
    private final ResumoOperacionalService resumoService;

    public TarefaController(TarefaService tarefaService, ResumoOperacionalService resumoService) {
        this.tarefaService = tarefaService;
        this.resumoService = resumoService;
    }

    @GetMapping
    public String listar(@ModelAttribute("filtro") TarefaFiltro filtro, Model model) {
        prepararListagem(model);
        model.addAttribute("pagina", tarefaService.listar(filtro));
        model.addAttribute("resumo", resumoService.resumo());
        return "tarefas/lista";
    }

    @GetMapping({"/nova", "/novo"})
    public String nova(Model model) {
        prepararFormulario(model, new TarefaRequest(), null);
        return "tarefas/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("tarefaForm") TarefaRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, request, null);
            return "tarefas/form";
        }
        try {
            TarefaDetalhe tarefa = tarefaService.criar(request, UsuarioAtor.de(authentication));
            attributes.addFlashAttribute("mensagem", "Tarefa criada com sucesso.");
            return "redirect:/sitio/tarefas/" + tarefa.id();
        } catch (TarefaAlertaOperacaoException ex) {
            bindingResult.addError(new ObjectError("tarefaForm", ex.getMessage()));
            prepararFormulario(model, request, null);
            return "tarefas/form";
        }
    }

    @GetMapping("/detalhe")
    public String aliasDetalhe() {
        return "redirect:/sitio/tarefas";
    }

    @GetMapping("/historico")
    public String aliasHistorico() {
        return "redirect:/sitio/tarefas?status=CONCLUIDA";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("active", "tarefas");
        model.addAttribute("tarefa", tarefaService.detalhar(id));
        return "tarefas/detalhe";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Authentication authentication, Model model,
            RedirectAttributes attributes) {
        try {
            prepararFormulario(model, tarefaService.formularioEdicao(id, UsuarioAtor.de(authentication)), id);
            return "tarefas/form";
        } catch (TarefaAlertaOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/sitio/tarefas/" + id;
        }
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id,
            @Valid @ModelAttribute("tarefaForm") TarefaRequest request,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, request, id);
            return "tarefas/form";
        }
        try {
            tarefaService.atualizar(id, request, UsuarioAtor.de(authentication));
            attributes.addFlashAttribute("mensagem", "Tarefa atualizada.");
        } catch (TarefaAlertaOperacaoException ex) {
            bindingResult.addError(new ObjectError("tarefaForm", ex.getMessage()));
            prepararFormulario(model, request, id);
            return "tarefas/form";
        }
        return "redirect:/sitio/tarefas/" + id;
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, Authentication authentication, RedirectAttributes attributes) {
        return executar(id, authentication, attributes, "Tarefa iniciada.",
                (tarefaId, ator) -> tarefaService.iniciar(tarefaId, ator));
    }

    @PostMapping("/{id}/concluir")
    public String concluir(@PathVariable Long id, Authentication authentication, RedirectAttributes attributes) {
        return executar(id, authentication, attributes, "Tarefa concluída.",
                (tarefaId, ator) -> tarefaService.concluir(tarefaId, ator));
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, Authentication authentication, RedirectAttributes attributes) {
        return executar(id, authentication, attributes, "Tarefa cancelada.",
                (tarefaId, ator) -> tarefaService.cancelar(tarefaId, ator));
    }

    @PostMapping("/{id}/recorrencia/desativar")
    public String desativarRecorrencia(@PathVariable Long id, Authentication authentication,
            RedirectAttributes attributes) {
        return executar(id, authentication, attributes, "Recorrência desativada.",
                (tarefaId, ator) -> tarefaService.desativarRecorrencia(tarefaId, ator));
    }

    private String executar(Long id, Authentication authentication, RedirectAttributes attributes,
            String sucesso, OperacaoTarefa operacao) {
        try {
            operacao.executar(id, UsuarioAtor.de(authentication));
            attributes.addFlashAttribute("mensagem", sucesso);
        } catch (TarefaAlertaOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/tarefas/" + id;
    }

    private void prepararListagem(Model model) {
        model.addAttribute("active", "tarefas");
        model.addAttribute("statusTarefas", StatusTarefa.values());
        model.addAttribute("prioridades", PrioridadeTarefa.values());
        model.addAttribute("prazos", PrazoTarefa.values());
        model.addAttribute("responsaveis", tarefaService.listarResponsaveisAtivos());
    }

    private void prepararFormulario(Model model, TarefaRequest request, Long tarefaId) {
        model.addAttribute("active", "tarefas");
        model.addAttribute("tarefaForm", request);
        model.addAttribute("tarefaId", tarefaId);
        model.addAttribute("prioridades", PrioridadeTarefa.values());
        model.addAttribute("recorrencias", TipoRecorrencia.values());
        model.addAttribute("responsaveis", tarefaService.listarResponsaveisAtivos());
    }

    @FunctionalInterface
    private interface OperacaoTarefa {
        TarefaDetalhe executar(Long id, UsuarioAtor ator);
    }
}
