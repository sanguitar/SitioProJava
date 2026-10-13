package com.example.sitiopro.frota.api;

import com.example.sitiopro.abastecimento.service.AbastecimentoService;
import com.example.sitiopro.frota.dto.*;
import com.example.sitiopro.frota.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/v1/frota")
public class FrotaApiController {
    private final VeiculoService veiculos;
    private final AbastecimentoService abastecimentos;
    private final FipeService fipe;

    public FrotaApiController(VeiculoService veiculos, AbastecimentoService abastecimentos, FipeService fipe) {
        this.veiculos = veiculos; this.abastecimentos = abastecimentos; this.fipe = fipe;
    }

    @GetMapping
    public List<VeiculoOperacionalResumo> listar() { return abastecimentos.resumoFrota(); }

    @GetMapping("/{id}")
    public VeiculoOperacionalResumo detalhar(@PathVariable Long id) { return abastecimentos.resumoOperacional(id); }

    @PostMapping
    public ResponseEntity<VeiculoResumo> criar(@Valid @RequestBody VeiculoRequest request) {
        var criado = veiculos.criar(request);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    public VeiculoResumo atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return veiculos.atualizar(id, request);
    }

    @GetMapping("/fipe/marcas")
    public List<FipeOpcaoResponse> marcas(@RequestParam Integer tipo) { return fipe.buscarMarcas(tipo); }

    @GetMapping("/fipe/modelos")
    public List<FipeOpcaoResponse> modelos(@RequestParam Integer tipo, @RequestParam Integer marca) {
        return fipe.buscarModelos(tipo, marca);
    }

    @GetMapping("/fipe/anos")
    public List<FipeAnoResponse> anos(@RequestParam Integer tipo, @RequestParam Integer marca,
            @RequestParam Integer modelo) { return fipe.buscarAnos(tipo, marca, modelo); }

    @GetMapping("/fipe/detalhes/{id}")
    public FipeDetalheResponse detalhes(@PathVariable Integer id) {
        FipeDetalheResponse detalhe = fipe.buscarDetalhes(id);
        if (detalhe == null) throw new FrotaOperacaoException("FIPE_NAO_ENCONTRADA", "Referência FIPE não encontrada.", org.springframework.http.HttpStatus.NOT_FOUND);
        return detalhe;
    }
}
