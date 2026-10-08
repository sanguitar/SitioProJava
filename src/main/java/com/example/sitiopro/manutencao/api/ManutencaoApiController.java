package com.example.sitiopro.manutencao.api;

import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.service.ManutencaoService;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.util.List;

@RestController
@RequestMapping("/api/v1/manutencao")
public class ManutencaoApiController {
    private final ManutencaoService service;
    private final com.example.sitiopro.manutencao.service.ManutencaoPreventivaService preventiva;
    public ManutencaoApiController(ManutencaoService service,
            com.example.sitiopro.manutencao.service.ManutencaoPreventivaService preventiva) {
        this.service = service; this.preventiva = preventiva;
    }

    @GetMapping
    public ManutencaoDashboardResumo dashboard() { return service.dashboard(); }
    @GetMapping("/ativos")
    public List<AtivoPatrimonialResumo> listarAtivos() { return service.listarAtivos(); }
    @GetMapping("/ativos/{id}")
    public AtivoPatrimonialResumo detalharAtivo(@PathVariable Long id) { return service.detalharAtivo(id); }
    @GetMapping("/ativos/{id}/manutencoes")
    public List<RegistroManutencaoResumo> listarManutencoes(@PathVariable Long id) { return service.listarManutencoes(id); }
    @GetMapping("/ativos/{id}/planos")
    public List<PlanoManutencaoPreventivaResumo> listarPlanos(@PathVariable Long id) { return preventiva.listarPlanos(id); }
    @GetMapping("/ativos/{id}/leituras")
    public List<LeituraMedidorResumo> listarLeituras(@PathVariable Long id) { return preventiva.listarLeituras(id); }

    @PostMapping("/ativos")
    public ResponseEntity<AtivoPatrimonialResumo> criarAtivo(@Valid @RequestBody AtivoPatrimonialRequest request) {
        AtivoPatrimonialResumo criado = service.criarAtivo(request);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/ativos/{id}")
    public AtivoPatrimonialResumo atualizarAtivo(@PathVariable Long id,
            @Valid @RequestBody AtivoPatrimonialRequest request) {
        return service.atualizarAtivo(id, request);
    }

    @PostMapping("/registros")
    public ResponseEntity<RegistroManutencaoResumo> registrar(@Valid @RequestBody RegistroManutencaoRequest request,
            Authentication authentication) {
        RegistroManutencaoResumo criado = service.registrarManutencao(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping("/registros/{id}")
    public RegistroManutencaoResumo detalhar(@PathVariable Long id) { return service.detalharManutencao(id); }

    @PostMapping("/registros/{id}/concluir-proxima")
    public RegistroManutencaoResumo concluirProxima(@PathVariable Long id, Authentication authentication) {
        return service.concluirProximaManutencao(id, UsuarioAtor.de(authentication));
    }

    @PostMapping("/planos")
    public ResponseEntity<PlanoManutencaoPreventivaResumo> criarPlano(
            @Valid @RequestBody PlanoManutencaoPreventivaRequest request, Authentication authentication) {
        var criado = preventiva.criarPlano(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/planos/{id}")
    public PlanoManutencaoPreventivaResumo atualizarPlano(@PathVariable Long id,
            @Valid @RequestBody PlanoManutencaoPreventivaRequest request, Authentication authentication) {
        return preventiva.atualizarPlano(id, request, UsuarioAtor.de(authentication));
    }

    @PostMapping("/planos/{id}/desativar")
    public PlanoManutencaoPreventivaResumo desativarPlano(@PathVariable Long id, @RequestParam long versao,
            Authentication authentication) {
        return preventiva.desativarPlano(id, versao, UsuarioAtor.de(authentication));
    }

    @PostMapping("/leituras")
    public ResponseEntity<LeituraMedidorResumo> registrarLeitura(@Valid @RequestBody LeituraMedidorRequest request,
            Authentication authentication) {
        var criada = preventiva.registrarLeitura(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PostMapping("/leituras/ajuste")
    public ResponseEntity<LeituraMedidorResumo> registrarAjuste(@Valid @RequestBody LeituraMedidorRequest request,
            Authentication authentication) {
        var criada = preventiva.registrarAjusteAdministrativo(request, UsuarioAtor.de(authentication));
        var uri = ServletUriComponentsBuilder.fromPath("/api/v1/manutencao/leituras/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }
}
