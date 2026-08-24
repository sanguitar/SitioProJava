package com.example.sitiopro.tarefas.controller;

import com.example.sitiopro.tarefas.dto.AlertaFiltro;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.ResumoOperacionalService;
import com.example.sitiopro.tarefas.service.TarefaAlertaOperacaoException;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sitio/alertas")
public class AlertaController {

    private final AlertaService alertaService;
    private final ResumoOperacionalService resumoService;

    public AlertaController(AlertaService alertaService, ResumoOperacionalService resumoService) {
        this.alertaService = alertaService;
        this.resumoService = resumoService;
    }

    @GetMapping
    public String listar(@ModelAttribute("filtro") AlertaFiltro filtro, Model model) {
        model.addAttribute("active", "alertas");
        model.addAttribute("pagina", alertaService.listar(filtro));
        model.addAttribute("resumo", resumoService.resumo());
        model.addAttribute("statusAlertas", StatusAlerta.values());
        model.addAttribute("severidades", SeveridadeAlerta.values());
        model.addAttribute("modulos", ModuloOrigem.values());
        return "alertas/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("active", "alertas");
        model.addAttribute("alerta", alertaService.detalhar(id));
        return "alertas/detalhe";
    }

    @PostMapping("/{id}/reconhecer")
    public String reconhecer(@PathVariable Long id, Authentication authentication,
            RedirectAttributes attributes) {
        return executar(id, authentication, attributes, "Alerta reconhecido.",
                (alertaId, ator) -> alertaService.reconhecer(alertaId, ator));
    }

    @PostMapping("/{id}/resolver")
    public String resolver(@PathVariable Long id, Authentication authentication,
            RedirectAttributes attributes) {
        return executar(id, authentication, attributes, "Alerta resolvido.",
                (alertaId, ator) -> alertaService.resolver(alertaId, ator));
    }

    @PostMapping("/{id}/criar-tarefa")
    public String criarTarefa(@PathVariable Long id, Authentication authentication,
            RedirectAttributes attributes) {
        try {
            TarefaDetalhe tarefa = alertaService.criarTarefa(id, UsuarioAtor.de(authentication));
            attributes.addFlashAttribute("mensagem", "Tarefa criada a partir do alerta.");
            return "redirect:/sitio/tarefas/" + tarefa.id();
        } catch (TarefaAlertaOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/sitio/alertas/" + id;
        }
    }

    private String executar(Long id, Authentication authentication, RedirectAttributes attributes,
            String sucesso, OperacaoAlerta operacao) {
        try {
            operacao.executar(id, UsuarioAtor.de(authentication));
            attributes.addFlashAttribute("mensagem", sucesso);
        } catch (TarefaAlertaOperacaoException ex) {
            attributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/sitio/alertas/" + id;
    }

    @FunctionalInterface
    private interface OperacaoAlerta {
        Object executar(Long id, UsuarioAtor ator);
    }
}
