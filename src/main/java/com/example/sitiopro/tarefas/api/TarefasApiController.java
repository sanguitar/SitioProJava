package com.example.sitiopro.tarefas.api;

import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.dto.TarefaDetalhe;
import com.example.sitiopro.tarefas.dto.TarefaFiltro;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.dto.TarefaResumo;
import com.example.sitiopro.tarefas.dto.TarefaResumoOperacional;
import com.example.sitiopro.tarefas.service.ResumoOperacionalService;
import com.example.sitiopro.tarefas.service.TarefaService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tarefas")
@Tag(name = "Tarefas", description = "Planejamento e execução de tarefas da propriedade")
public class TarefasApiController {

    private final TarefaService tarefaService;
    private final ResumoOperacionalService resumoService;

    public TarefasApiController(TarefaService tarefaService, ResumoOperacionalService resumoService) {
        this.tarefaService = tarefaService;
        this.resumoService = resumoService;
    }

    @GetMapping
    @Operation(summary = "Lista tarefas com filtros e paginação")
    public PaginaResponse<TarefaResumo> listar(@ModelAttribute TarefaFiltro filtro) {
        return tarefaService.listar(filtro);
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resume pendências e alertas ativos")
    public TarefaResumoOperacional resumo() {
        return resumoService.resumo();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma tarefa e seu histórico")
    public TarefaDetalhe detalhar(@PathVariable Long id) {
        return tarefaService.detalhar(id);
    }

    @PostMapping
    @Operation(summary = "Cria uma tarefa manual")
    public ResponseEntity<TarefaDetalhe> criar(@Valid @RequestBody TarefaRequest request,
            Authentication authentication) {
        TarefaDetalhe tarefa = tarefaService.criar(request, UsuarioAtor.de(authentication));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(tarefa.id()).toUri();
        return ResponseEntity.created(location).body(tarefa);
    }

    @PostMapping("/{id}/iniciar")
    @Operation(summary = "Inicia uma tarefa pendente")
    public TarefaDetalhe iniciar(@PathVariable Long id, Authentication authentication) {
        return tarefaService.iniciar(id, UsuarioAtor.de(authentication));
    }

    @PostMapping("/{id}/concluir")
    @Operation(summary = "Conclui uma tarefa")
    public TarefaDetalhe concluir(@PathVariable Long id, Authentication authentication) {
        return tarefaService.concluir(id, UsuarioAtor.de(authentication));
    }
}
