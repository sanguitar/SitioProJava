package com.example.sitiopro.propriedade.api;

import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.propriedade.service.PerimetroService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/propriedade")
public class PropriedadeApiController {
    private final PropriedadeService service;
    private final PerimetroService perimetros;
    public PropriedadeApiController(PropriedadeService service, PerimetroService perimetros) {
        this.service = service; this.perimetros = perimetros;
    }
    @GetMapping("/perimetro")
    public PerimetroResumo perimetro() { return perimetros.obter(); }
    @PutMapping("/perimetro")
    public PerimetroResumo salvarPerimetro(@Valid @RequestBody PerimetroRequest request) {
        return perimetros.salvar(request);
    }
    @GetMapping("/resumo")
    public PropriedadeResumo resumo() { return service.resumo(); }
    @PutMapping
    public PropriedadeResumo atualizar(@Valid @RequestBody PropriedadeRequest request) { return service.atualizar(request); }

    @GetMapping("/areas")
    public PaginaResponse<CadastroFisicoResumo> listarAreaPropriedade(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return service.listarAreaPropriedade(pagina, tamanho); }

    @GetMapping("/areas/{id}")
    public CadastroFisicoResumo detalharAreaPropriedade(@PathVariable Long id) { return service.detalharAreaPropriedade(id); }

    @PostMapping("/areas")
    public ResponseEntity<CadastroFisicoResumo> criarAreaPropriedade(@Valid @RequestBody AreaPropriedadeRequest request) {
        var criado = service.salvarAreaPropriedade(null, request);
        return ResponseEntity.created(URI.create("/api/v1/propriedade/areas/" + criado.id())).body(criado);
    }

    @PutMapping("/areas/{id}")
    public CadastroFisicoResumo atualizarAreaPropriedade(@PathVariable Long id, @Valid @RequestBody AreaPropriedadeRequest request) {
        return service.salvarAreaPropriedade(id, request);
    }

    @PostMapping("/areas/{id}/desativar")
    public ResponseEntity<Void> desativarAreaPropriedade(@PathVariable Long id, @Valid @RequestBody VersaoRequest request) {
        service.desativarAreaPropriedade(id, request.versao());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/talhoes")
    public PaginaResponse<CadastroFisicoResumo> listarTalhao(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return service.listarTalhao(pagina, tamanho); }

    @GetMapping("/talhoes/geojson")
    public TalhoesGeoJsonResumo geoJsonTalhoes() { return service.geoJsonTalhoes(); }

    @GetMapping("/talhoes/{id}")
    public CadastroFisicoResumo detalharTalhao(@PathVariable Long id) { return service.detalharTalhao(id); }

    @PostMapping("/talhoes")
    public ResponseEntity<CadastroFisicoResumo> criarTalhao(@Valid @RequestBody TalhaoRequest request) {
        var criado = service.salvarTalhao(null, request);
        return ResponseEntity.created(URI.create("/api/v1/propriedade/talhoes/" + criado.id())).body(criado);
    }

    @PutMapping("/talhoes/{id}")
    public CadastroFisicoResumo atualizarTalhao(@PathVariable Long id, @Valid @RequestBody TalhaoRequest request) {
        return service.salvarTalhao(id, request);
    }

    @PostMapping("/talhoes/{id}/desativar")
    public ResponseEntity<Void> desativarTalhao(@PathVariable Long id, @Valid @RequestBody VersaoRequest request) {
        service.desativarTalhao(id, request.versao());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/piquetes")
    public PaginaResponse<CadastroFisicoResumo> listarPiquete(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return service.listarPiquete(pagina, tamanho); }

    @GetMapping("/piquetes/{id}")
    public CadastroFisicoResumo detalharPiquete(@PathVariable Long id) { return service.detalharPiquete(id); }

    @PostMapping("/piquetes")
    public ResponseEntity<CadastroFisicoResumo> criarPiquete(@Valid @RequestBody PiqueteRequest request) {
        var criado = service.salvarPiquete(null, request);
        return ResponseEntity.created(URI.create("/api/v1/propriedade/piquetes/" + criado.id())).body(criado);
    }

    @PutMapping("/piquetes/{id}")
    public CadastroFisicoResumo atualizarPiquete(@PathVariable Long id, @Valid @RequestBody PiqueteRequest request) {
        return service.salvarPiquete(id, request);
    }

    @PostMapping("/piquetes/{id}/desativar")
    public ResponseEntity<Void> desativarPiquete(@PathVariable Long id, @Valid @RequestBody VersaoRequest request) {
        service.desativarPiquete(id, request.versao());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estruturas")
    public PaginaResponse<CadastroFisicoResumo> listarEstruturaPropriedade(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return service.listarEstruturaPropriedade(pagina, tamanho); }

    @GetMapping("/estruturas/{id}")
    public CadastroFisicoResumo detalharEstruturaPropriedade(@PathVariable Long id) { return service.detalharEstruturaPropriedade(id); }

    @PostMapping("/estruturas")
    public ResponseEntity<CadastroFisicoResumo> criarEstruturaPropriedade(@Valid @RequestBody EstruturaPropriedadeRequest request) {
        var criado = service.salvarEstruturaPropriedade(null, request);
        return ResponseEntity.created(URI.create("/api/v1/propriedade/estruturas/" + criado.id())).body(criado);
    }

    @PutMapping("/estruturas/{id}")
    public CadastroFisicoResumo atualizarEstruturaPropriedade(@PathVariable Long id, @Valid @RequestBody EstruturaPropriedadeRequest request) {
        return service.salvarEstruturaPropriedade(id, request);
    }

    @PostMapping("/estruturas/{id}/desativar")
    public ResponseEntity<Void> desativarEstruturaPropriedade(@PathVariable Long id, @Valid @RequestBody VersaoRequest request) {
        service.desativarEstruturaPropriedade(id, request.versao());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recursos-hidricos")
    public PaginaResponse<CadastroFisicoResumo> listarRecursoHidrico(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return service.listarRecursoHidrico(pagina, tamanho); }

    @GetMapping("/recursos-hidricos/{id}")
    public CadastroFisicoResumo detalharRecursoHidrico(@PathVariable Long id) { return service.detalharRecursoHidrico(id); }

    @PostMapping("/recursos-hidricos")
    public ResponseEntity<CadastroFisicoResumo> criarRecursoHidrico(@Valid @RequestBody RecursoHidricoRequest request) {
        var criado = service.salvarRecursoHidrico(null, request);
        return ResponseEntity.created(URI.create("/api/v1/propriedade/recursos-hidricos/" + criado.id())).body(criado);
    }

    @PutMapping("/recursos-hidricos/{id}")
    public CadastroFisicoResumo atualizarRecursoHidrico(@PathVariable Long id, @Valid @RequestBody RecursoHidricoRequest request) {
        return service.salvarRecursoHidrico(id, request);
    }

    @PostMapping("/recursos-hidricos/{id}/desativar")
    public ResponseEntity<Void> desativarRecursoHidrico(@PathVariable Long id, @Valid @RequestBody VersaoRequest request) {
        service.desativarRecursoHidrico(id, request.versao());
        return ResponseEntity.noContent().build();
    }
}
