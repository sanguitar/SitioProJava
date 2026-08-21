package com.example.sitiopro.compras.api;

import com.example.sitiopro.compras.dto.FornecedorRequest;
import com.example.sitiopro.compras.dto.FornecedorResumo;
import com.example.sitiopro.compras.service.FornecedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fornecedores")
@Tag(name = "Fornecedores", description = "Fornecedores usados pelo módulo de compras")
public class FornecedoresApiController {

    private final FornecedorService fornecedorService;

    public FornecedoresApiController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
    }

    @GetMapping
    @Operation(summary = "Lista fornecedores")
    public List<FornecedorResumo> listar() {
        return fornecedorService.listarTodos();
    }

    @PostMapping
    @Operation(summary = "Cria fornecedor")
    public ResponseEntity<FornecedorResumo> criar(@Valid @RequestBody FornecedorRequest request) {
        FornecedorResumo fornecedor = fornecedorService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(fornecedor.id())
                .toUri();
        return ResponseEntity.created(location).body(fornecedor);
    }
}
