package com.example.sitiopro.criacao.peixes.api;

import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.service.PeixesSanidadeService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/v1/criacoes/peixes/sanidade")
public class PeixesSanidadeApiController {
    private final PeixesSanidadeService sanidade;
    public PeixesSanidadeApiController(PeixesSanidadeService sanidade) { this.sanidade = sanidade; }

    @GetMapping
    public List<RegistroSanitarioPeixesResumo> listar(@RequestParam(required = false) Long loteId) {
        return sanidade.listar(loteId);
    }

    @GetMapping("/{id}")
    public RegistroSanitarioPeixesResumo detalhar(@PathVariable Long id) { return sanidade.detalhar(id); }

    @PostMapping
    public ResponseEntity<RegistroSanitarioPeixesResumo> registrar(
            @Valid @RequestBody RegistroSanitarioPeixesRequest request, Authentication authentication) {
        RegistroSanitarioPeixesResumo criado = sanidade.registrar(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PostMapping("/{id}/concluir-proxima-acao")
    public RegistroSanitarioPeixesResumo concluirProximaAcao(@PathVariable Long id,
            Authentication authentication) {
        return sanidade.concluirProximaAcao(id, UsuarioAtor.de(authentication));
    }
}
