package com.example.sitiopro.estoque.api;

import com.example.sitiopro.estoque.dto.EstoqueDashboardResumo;
import com.example.sitiopro.estoque.dto.ItemEstoqueDetalhe;
import com.example.sitiopro.estoque.dto.ItemEstoqueRequest;
import com.example.sitiopro.estoque.dto.ItemEstoqueResumo;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueRequest;
import com.example.sitiopro.estoque.dto.MovimentoEstoqueResponse;
import com.example.sitiopro.estoque.entity.ItemEstoque;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/estoque")
@Tag(name = "Estoque", description = "Consulta e movimentação inicial do estoque da propriedade")
public class EstoqueApiController {

    private final EstoqueCatalogoService catalogoService;
    private final EstoqueMovimentoService movimentoService;

    public EstoqueApiController(EstoqueCatalogoService catalogoService, EstoqueMovimentoService movimentoService) {
        this.catalogoService = catalogoService;
        this.movimentoService = movimentoService;
    }

    @GetMapping("/resumo")
    @Operation(summary = "Resumo operacional do estoque")
    public EstoqueDashboardResumo resumo() {
        return movimentoService.montarResumo();
    }

    @GetMapping("/itens")
    @Operation(summary = "Lista itens de estoque com saldo calculado")
    public List<ItemEstoqueResumo> itens() {
        return movimentoService.listarItensComSaldo();
    }

    @GetMapping("/itens/{id}")
    @Operation(summary = "Detalha um item de estoque")
    public ItemEstoqueDetalhe item(@PathVariable Long id) {
        return movimentoService.detalharItem(id);
    }

    @PostMapping("/itens")
    @Operation(summary = "Cria item de estoque")
    public ResponseEntity<ItemEstoqueDetalhe> criarItem(@Valid @RequestBody ItemEstoqueRequest request) {
        ItemEstoque criado = catalogoService.criarItem(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(movimentoService.detalharItem(criado.getId()));
    }

    @GetMapping("/movimentos")
    @Operation(summary = "Lista o histórico de movimentações")
    public List<MovimentoEstoqueResponse> movimentos() {
        return movimentoService.listarMovimentos();
    }

    @GetMapping("/movimentos/{id}")
    @Operation(summary = "Detalha uma movimentação de estoque")
    public MovimentoEstoqueResponse movimento(@PathVariable Long id) {
        return movimentoService.buscarMovimento(id);
    }

    @PostMapping("/movimentos")
    @Operation(summary = "Registra uma movimentação de estoque")
    public ResponseEntity<MovimentoEstoqueResponse> registrarMovimento(
            @Valid @RequestBody MovimentoEstoqueRequest request,
            Authentication authentication) {
        MovimentoEstoqueResponse response = movimentoService.registrarMovimento(request,
                usuarioPodeAjustar(authentication));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    private boolean usuarioPodeAjustar(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
