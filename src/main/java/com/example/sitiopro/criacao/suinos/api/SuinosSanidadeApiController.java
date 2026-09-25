package com.example.sitiopro.criacao.suinos.api;

import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.service.SuinosSanidadeService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/criacoes/suinos/sanidade")
public class SuinosSanidadeApiController {
    private final SuinosSanidadeService sanidade;

    public SuinosSanidadeApiController(SuinosSanidadeService sanidade) {
        this.sanidade = sanidade;
    }

    @GetMapping
    public List<RegistroSanitarioSuinosResumo> listar(@RequestParam(required = false) Long loteId,
            @RequestParam(required = false) Long animalId) {
        return sanidade.listar(loteId, animalId);
    }

    @GetMapping("/{id}")
    public RegistroSanitarioSuinosResumo detalhar(@PathVariable Long id) {
        return sanidade.detalhar(id);
    }

    @PostMapping
    public ResponseEntity<RegistroSanitarioSuinosResumo> registrar(
            @Valid @RequestBody RegistroSanitarioSuinosRequest request, Authentication authentication) {
        RegistroSanitarioSuinosResumo criado = sanidade.registrar(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PostMapping("/{id}/concluir-proxima-acao")
    public RegistroSanitarioSuinosResumo concluirProximaAcao(@PathVariable Long id,
            Authentication authentication) {
        return sanidade.concluirProximaAcao(id, UsuarioAtor.de(authentication));
    }
}
