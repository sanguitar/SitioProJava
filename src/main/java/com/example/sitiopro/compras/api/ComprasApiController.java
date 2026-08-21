package com.example.sitiopro.compras.api;

import com.example.sitiopro.compras.dto.CompraDetalhe;
import com.example.sitiopro.compras.dto.CompraFiltro;
import com.example.sitiopro.compras.dto.CompraRequest;
import com.example.sitiopro.compras.dto.CompraResumo;
import com.example.sitiopro.compras.dto.ItemCompraRequest;
import com.example.sitiopro.compras.service.CompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/compras")
@Tag(name = "Compras", description = "Compras, itens de compra e confirmação com entrada no estoque")
public class ComprasApiController {

    private final CompraService compraService;

    public ComprasApiController(CompraService compraService) {
        this.compraService = compraService;
    }

    @GetMapping
    @Operation(summary = "Lista compras com filtros opcionais")
    public List<CompraResumo> listar(@ModelAttribute CompraFiltro filtro) {
        return compraService.listar(filtro);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha uma compra")
    public CompraDetalhe detalhar(@PathVariable Long id) {
        return compraService.detalhar(id);
    }

    @PostMapping
    @Operation(summary = "Cria uma compra em rascunho")
    public ResponseEntity<CompraDetalhe> criar(@Valid @RequestBody CompraRequest request) {
        CompraDetalhe compra = compraService.criarCompra(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(compra.id())
                .toUri();
        return ResponseEntity.created(location).body(compra);
    }

    @PostMapping("/{id}/itens")
    @Operation(summary = "Adiciona item em uma compra rascunho")
    public CompraDetalhe adicionarItem(@PathVariable Long id, @Valid @RequestBody ItemCompraRequest request) {
        return compraService.adicionarItem(id, request);
    }

    @PostMapping("/{id}/confirmar")
    @Operation(summary = "Confirma compra e registra entradas de estoque em uma transação")
    public CompraDetalhe confirmar(@PathVariable Long id) {
        return compraService.confirmarCompra(id);
    }
}
