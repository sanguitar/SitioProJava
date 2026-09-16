package com.example.sitiopro.criacao.aves.api;

import com.example.sitiopro.criacao.aves.dto.*;
import com.example.sitiopro.criacao.aves.entity.StatusLoteAves;
import com.example.sitiopro.criacao.aves.service.*;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoRequest;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoResumo;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/criacoes/aves")
@Tag(name = "Criações - Aves", description = "Instalações, lotes, manejo e incubação de aves")
public class AvesApiController {
    private final AvesResumoService resumoService;
    private final InstalacaoCriacaoService instalacaoService;
    private final LoteAvesService loteService;
    private final ManejoAvesService manejoService;
    private final IncubacaoAvesService incubacaoService;
    private final IncubacaoAcompanhamentoService acompanhamentoService;
    private final OvoscopiaIncubacaoAvesService ovoscopiaService;

    public AvesApiController(AvesResumoService resumoService, InstalacaoCriacaoService instalacaoService,
            LoteAvesService loteService, ManejoAvesService manejoService, IncubacaoAvesService incubacaoService,
            IncubacaoAcompanhamentoService acompanhamentoService,
            OvoscopiaIncubacaoAvesService ovoscopiaService) {
        this.resumoService = resumoService; this.instalacaoService = instalacaoService;
        this.loteService = loteService; this.manejoService = manejoService; this.incubacaoService = incubacaoService;
        this.acompanhamentoService = acompanhamentoService;
        this.ovoscopiaService = ovoscopiaService;
    }

    @GetMapping("/resumo") @Operation(summary = "Resumo operacional de aves")
    public AvesResumo resumo() { return resumoService.resumo(); }

    @GetMapping("/instalacoes") @Operation(summary = "Lista instalações paginadas")
    public PaginaResponse<InstalacaoCriacaoResumo> instalacoes(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return instalacaoService.listar(pagina, tamanho); }

    @GetMapping("/instalacoes/{id}")
    public InstalacaoCriacaoResumo instalacao(@PathVariable Long id) { return instalacaoService.detalhar(id); }

    @PostMapping("/instalacoes")
    public ResponseEntity<InstalacaoCriacaoResumo> criarInstalacao(@Valid @RequestBody InstalacaoCriacaoRequest request) {
        InstalacaoCriacaoResumo criado = instalacaoService.criar(request);
        return ResponseEntity.created(location("/api/v1/criacoes/aves/instalacoes/{id}", criado.id())).body(criado);
    }

    @GetMapping("/lotes") @Operation(summary = "Lista lotes paginados")
    public PaginaResponse<LoteAvesResumo> lotes(@RequestParam(required = false) StatusLoteAves status,
            @RequestParam(required = false) String termo, @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return loteService.listar(status, termo, pagina, tamanho); }

    @GetMapping("/lotes/{id}")
    public LoteAvesDetalhe lote(@PathVariable Long id) { return loteService.detalhar(id); }

    @PostMapping("/lotes")
    public ResponseEntity<LoteAvesDetalhe> criarLote(@Valid @RequestBody CriarLoteAvesRequest request,
            Authentication authentication) {
        LoteAvesDetalhe criado = loteService.criar(request, UsuarioAtor.de(authentication));
        return ResponseEntity.created(location("/api/v1/criacoes/aves/lotes/{id}", criado.id())).body(criado);
    }

    @PostMapping("/lotes/{id}/alimentacoes")
    public LoteAvesDetalhe alimentacao(@PathVariable Long id, @Valid @RequestBody RegistrarAlimentacaoAvesRequest request,
            Authentication authentication) { return manejoService.registrarAlimentacao(id, request, UsuarioAtor.de(authentication)); }

    @PostMapping("/lotes/{id}/mortalidades")
    public LoteAvesDetalhe mortalidade(@PathVariable Long id, @Valid @RequestBody RegistrarMortalidadeAvesRequest request,
            Authentication authentication) { return manejoService.registrarMortalidade(id, request, UsuarioAtor.de(authentication)); }

    @PostMapping("/lotes/{id}/pesagens")
    public LoteAvesDetalhe pesagem(@PathVariable Long id, @Valid @RequestBody RegistrarPesagemAvesRequest request,
            Authentication authentication) { return manejoService.registrarPesagem(id, request, UsuarioAtor.de(authentication)); }

    @PostMapping("/lotes/{id}/posturas")
    public LoteAvesDetalhe postura(@PathVariable Long id, @Valid @RequestBody RegistrarPosturaAvesRequest request,
            Authentication authentication) { return manejoService.registrarPostura(id, request, UsuarioAtor.de(authentication)); }

    @PostMapping("/lotes/{id}/transferencias")
    public LoteAvesDetalhe transferencia(@PathVariable Long id, @Valid @RequestBody TransferirLoteAvesRequest request,
            Authentication authentication) { return manejoService.transferir(id, request, UsuarioAtor.de(authentication)); }

    @GetMapping("/incubacoes")
    public PaginaResponse<IncubacaoAvesResumo> incubacoes(@RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) { return incubacaoService.listar(pagina, tamanho); }

    @GetMapping("/incubacoes/{id}")
    public IncubacaoAvesDetalhe incubacao(@PathVariable Long id) { return incubacaoService.detalhar(id); }

    @PostMapping("/incubacoes")
    public ResponseEntity<IncubacaoAvesDetalhe> criarIncubacao(@Valid @RequestBody CriarIncubacaoAvesRequest request,
            Authentication authentication) {
        IncubacaoAvesDetalhe criada = incubacaoService.criar(request, UsuarioAtor.de(authentication));
        return ResponseEntity.created(location("/api/v1/criacoes/aves/incubacoes/{id}", criada.id())).body(criada);
    }

    @PostMapping("/incubacoes/{id}/finalizar")
    public IncubacaoAvesDetalhe finalizar(@PathVariable Long id,
            @Valid @RequestBody FinalizarIncubacaoAvesRequest request, Authentication authentication) {
        return incubacaoService.finalizar(id, request, UsuarioAtor.de(authentication));
    }

    @GetMapping("/incubacoes/{id}/acompanhamentos")
    public java.util.List<AcompanhamentoIncubacaoAvesResumo> acompanhamentos(@PathVariable Long id) {
        incubacaoService.detalhar(id);
        return acompanhamentoService.listar(id);
    }

    @GetMapping("/incubacoes/{id}/acompanhamentos/{acompanhamentoId}")
    public AcompanhamentoIncubacaoAvesResumo acompanhamento(@PathVariable Long id,
            @PathVariable Long acompanhamentoId) {
        return acompanhamentoService.detalhar(id, acompanhamentoId);
    }

    @PostMapping("/incubacoes/{id}/acompanhamentos")
    public ResponseEntity<AcompanhamentoIncubacaoAvesResumo> registrarAcompanhamento(@PathVariable Long id,
            @Valid @RequestBody RegistrarAcompanhamentoIncubacaoAvesRequest request) {
        AcompanhamentoIncubacaoAvesResumo criado = acompanhamentoService.registrar(id, request);
        return ResponseEntity.created(location(
                "/api/v1/criacoes/aves/incubacoes/{id}/acompanhamentos/{acompanhamentoId}", id,
                criado.id())).body(criado);
    }

    @GetMapping("/incubacoes/{id}/ovoscopias")
    public java.util.List<OvoscopiaIncubacaoAvesResumo> ovoscopias(@PathVariable Long id) {
        incubacaoService.detalhar(id);
        return ovoscopiaService.ovoscopias(id);
    }

    @PostMapping("/incubacoes/{id}/ovoscopias")
    public ResponseEntity<OvoscopiaIncubacaoAvesResumo> registrarOvoscopia(@PathVariable Long id,
            @Valid @RequestBody RegistrarOvoscopiaIncubacaoAvesRequest request, Authentication authentication) {
        OvoscopiaIncubacaoAvesResumo criada = ovoscopiaService.registrar(id, request, UsuarioAtor.de(authentication));
        return ResponseEntity.created(location(
                "/api/v1/criacoes/aves/incubacoes/{id}/ovoscopias/{ovoscopiaId}", id,
                criada.id())).body(criada);
    }

    @PostMapping("/incubacoes/{id}/ajustar-previsao")
    public IncubacaoAvesDetalhe ajustarPrevisao(@PathVariable Long id,
            @Valid @RequestBody AjustarPrevisaoIncubacaoAvesRequest request, Authentication authentication) {
        return incubacaoService.ajustarPrevisao(id, request, UsuarioAtor.de(authentication));
    }

    @PostMapping("/incubacoes/{id}/cancelar")
    public IncubacaoAvesDetalhe cancelarIncubacao(@PathVariable Long id, Authentication authentication) {
        return incubacaoService.cancelar(id, UsuarioAtor.de(authentication));
    }

    private URI location(String path, Long id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).buildAndExpand(id).toUri();
    }

    private URI location(String path, Long id, Long nestedId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(path)
                .buildAndExpand(id, nestedId).toUri();
    }
}
