package com.example.sitiopro.criacao.peixes.api;
import com.example.sitiopro.criacao.peixes.dto.*;
import com.example.sitiopro.criacao.peixes.entity.StatusLotePeixes;
import com.example.sitiopro.criacao.peixes.service.PeixesService;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
@RestController
@RequestMapping("/api/v1/criacoes/peixes")
public class PeixesApiController {
    private final PeixesService service; public PeixesApiController(PeixesService service){this.service=service;}
    @GetMapping("/resumo") public PeixesDashboardResumo resumo(){return service.dashboard();}
    @GetMapping("/lotes") public PaginaResponse<LotePeixesResumo> lotes(@RequestParam(required=false) StatusLotePeixes status,@RequestParam(required=false) String termo,@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="20") int tamanho){return service.listar(status,termo,pagina,tamanho);}
    @GetMapping("/lotes/{id}") public LotePeixesDetalhe lote(@PathVariable Long id){return service.detalhar(id);}
    @PostMapping("/lotes") public ResponseEntity<LotePeixesDetalhe> criar(@Valid @RequestBody CriarLotePeixesRequest r,Authentication a){var criado=service.criar(r,UsuarioAtor.de(a));var uri=ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/criacoes/peixes/lotes/{id}").buildAndExpand(criado.id()).toUri();return ResponseEntity.created(uri).body(criado);}
    @PostMapping("/lotes/{id}/entradas") public LotePeixesDetalhe entrada(@PathVariable Long id,@Valid @RequestBody EntradaPeixesRequest r,Authentication a){return service.registrarEntrada(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/perdas") public LotePeixesDetalhe perda(@PathVariable Long id,@Valid @RequestBody PerdaPeixesRequest r,Authentication a){return service.registrarPerda(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/transferencias") public LotePeixesDetalhe transferencia(@PathVariable Long id,@Valid @RequestBody TransferenciaPeixesRequest r,Authentication a){return service.transferir(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/biometrias") public LotePeixesDetalhe biometria(@PathVariable Long id,@Valid @RequestBody BiometriaPeixesRequest r,Authentication a){return service.registrarBiometria(id,r,UsuarioAtor.de(a));}
    @PostMapping("/lotes/{id}/alimentacoes") public LotePeixesDetalhe alimentacao(@PathVariable Long id,@Valid @RequestBody AlimentacaoPeixesRequest r,Authentication a){return service.registrarAlimentacao(id,r,UsuarioAtor.de(a));}
}
