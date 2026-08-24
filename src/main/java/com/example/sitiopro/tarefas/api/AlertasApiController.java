package com.example.sitiopro.tarefas.api;

import com.example.sitiopro.tarefas.dto.AlertaDetalhe;
import com.example.sitiopro.tarefas.dto.AlertaFiltro;
import com.example.sitiopro.tarefas.dto.AlertaResumo;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.service.AlertaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alertas")
@Tag(name = "Alertas", description = "Alertas operacionais automáticos e seu ciclo de tratamento")
public class AlertasApiController {

    private final AlertaService alertaService;

    public AlertasApiController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    @Operation(summary = "Lista alertas com filtros e paginação")
    public PaginaResponse<AlertaResumo> listar(@ModelAttribute AlertaFiltro filtro) {
        return alertaService.listar(filtro);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um alerta e seu histórico")
    public AlertaDetalhe detalhar(@PathVariable Long id) {
        return alertaService.detalhar(id);
    }

    @PostMapping("/{id}/reconhecer")
    @Operation(summary = "Reconhece um alerta ativo")
    public AlertaDetalhe reconhecer(@PathVariable Long id, Authentication authentication) {
        return alertaService.reconhecer(id, UsuarioAtor.de(authentication));
    }

    @PostMapping("/{id}/criar-tarefa")
    @Operation(summary = "Cria uma tarefa vinculada ao alerta")
    public TarefaDetalhe criarTarefa(@PathVariable Long id, Authentication authentication) {
        return alertaService.criarTarefa(id, UsuarioAtor.de(authentication));
    }
}
