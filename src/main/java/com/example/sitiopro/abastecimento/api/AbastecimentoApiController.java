package com.example.sitiopro.abastecimento.api;

import com.example.sitiopro.abastecimento.dto.*;
import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/abastecimentos")
public class AbastecimentoApiController {
    private final AbastecimentoService service;
    public AbastecimentoApiController(AbastecimentoService service) { this.service = service; }

    @GetMapping
    public PaginaResponse<AbastecimentoResumo> listar(@RequestParam(required = false) Long veiculoId,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(veiculoId, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public AbastecimentoResumo detalhar(@PathVariable Long id) { return service.detalhar(id); }

    @PostMapping
    public ResponseEntity<AbastecimentoResumo> registrar(@Valid @RequestBody AbastecimentoRequest request,
            Authentication authentication) {
        var criado = service.registrar(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }
}
