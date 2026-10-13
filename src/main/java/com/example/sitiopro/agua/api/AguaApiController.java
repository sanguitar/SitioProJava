package com.example.sitiopro.agua.api;

import com.example.sitiopro.agua.dto.*;
import com.example.sitiopro.agua.service.AguaService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/agua")
public class AguaApiController {
    private final AguaService service;

    public AguaApiController(AguaService service) {
        this.service = service;
    }

    @GetMapping
    public AguaDashboardResumo dashboard() { return service.dashboard(); }

    @GetMapping("/recursos")
    public List<RecursoHidricoOperacionalResumo> recursos() { return service.listarRecursos(); }

    @GetMapping("/recursos/{id}")
    public RecursoHidricoOperacionalResumo recurso(@PathVariable Long id) { return service.detalharRecurso(id); }

    @GetMapping("/recursos/{id}/operacoes")
    public List<OperacaoAguaResumo> operacoesDoRecurso(@PathVariable Long id) {
        return service.listarOperacoesDoRecurso(id);
    }

    @GetMapping("/operacoes")
    public PaginaResponse<OperacaoAguaResumo> operacoes(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "30") int tamanho) {
        return service.listarOperacoes(pagina, tamanho);
    }

    @PostMapping("/operacoes")
    public ResponseEntity<OperacaoAguaResumo> registrar(@Valid @RequestBody OperacaoAguaRequest request,
            Authentication authentication) {
        OperacaoAguaResumo criada = service.registrar(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/recursos/{id}/configuracao")
    public RecursoHidricoOperacionalResumo configurar(@PathVariable Long id,
            @Valid @RequestBody RecursoHidricoOperacaoConfigRequest request, Authentication authentication) {
        return service.configurar(id, request, UsuarioAtor.de(authentication));
    }
}
