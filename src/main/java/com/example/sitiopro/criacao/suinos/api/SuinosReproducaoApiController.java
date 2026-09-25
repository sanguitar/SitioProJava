package com.example.sitiopro.criacao.suinos.api;

import com.example.sitiopro.criacao.suinos.dto.*;
import com.example.sitiopro.criacao.suinos.service.SuinosReproducaoService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/v1/criacoes/suinos/reproducao")
public class SuinosReproducaoApiController {
    private final SuinosReproducaoService service;
    public SuinosReproducaoApiController(SuinosReproducaoService service) { this.service = service; }
    @GetMapping("/resumo") public ReproducaoSuinosResumo resumo() { return service.resumoOperacional(); }
    @GetMapping("/animais") public List<AnimalReprodutivoSuinosResumo> animais() { return service.listarAnimais(); }
    @GetMapping("/animais/{id}") public AnimalReprodutivoSuinosResumo animal(@PathVariable Long id) { return service.detalharAnimal(id); }
    @GetMapping("/animais/{id}/historico") public List<CicloReprodutivoSuinosResumo> historico(@PathVariable Long id) { return service.historicoMatriz(id); }
    @GetMapping("/ciclos") public List<CicloReprodutivoSuinosResumo> ciclos() { return service.listarCiclos(); }
    @GetMapping("/ciclos/{id}") public CicloReprodutivoSuinosDetalhe ciclo(@PathVariable Long id) { return service.detalhar(id); }
    @PostMapping("/animais") public ResponseEntity<AnimalReprodutivoSuinosResumo> animal(@Valid @RequestBody CriarAnimalReprodutivoRequest request, Authentication auth) {
        var criado = service.cadastrarAnimal(request, UsuarioAtor.de(auth));
        var uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/criacoes/suinos/reproducao/animais/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }
    @PostMapping("/ciclos") public ResponseEntity<CicloReprodutivoSuinosDetalhe> cobertura(@Valid @RequestBody RegistrarCoberturaSuinosRequest request, Authentication auth) {
        var criado = service.registrarCobertura(request, UsuarioAtor.de(auth));
        var uri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/criacoes/suinos/reproducao/ciclos/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }
    @PostMapping("/ciclos/{id}/checagem") public CicloReprodutivoSuinosDetalhe checagem(@PathVariable Long id, @Valid @RequestBody ChecagemGestacaoSuinosRequest request, Authentication auth) { return service.registrarChecagem(id, request, UsuarioAtor.de(auth)); }
    @PostMapping("/ciclos/{id}/parto") public CicloReprodutivoSuinosDetalhe parto(@PathVariable Long id, @Valid @RequestBody RegistrarPartoSuinosRequest request, Authentication auth) { return service.registrarParto(id, request, UsuarioAtor.de(auth)); }
    @PostMapping("/ciclos/{id}/desmame") public CicloReprodutivoSuinosDetalhe desmame(@PathVariable Long id, @Valid @RequestBody RegistrarDesmameSuinosRequest request, Authentication auth) { return service.registrarDesmame(id, request, UsuarioAtor.de(auth)); }
}
