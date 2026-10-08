package com.example.sitiopro.manutencao.service;

import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.estoque.entity.*;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.manutencao.dto.*;
import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.*;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.tarefas.service.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManutencaoServiceTests {
    @Mock AtivoPatrimonialRepository ativos;
    @Mock RegistroManutencaoRepository registros;
    @Mock EstruturaPropriedadeRepository estruturas;
    @Mock PropriedadeRepository propriedades;
    @Mock EstoqueCatalogoService catalogo;
    @Mock EstoqueMovimentoService estoque;
    @Mock CodigoCriacaoService codigos;
    @Mock TarefaService tarefas;
    @Mock ManutencaoAlertasService alertas;
    @Mock ManutencaoPreventivaService preventiva;
    ManutencaoService service;
    AtivoPatrimonial ativo;
    final UsuarioAtor operador = new UsuarioAtor(2L, "operador", false);

    @BeforeEach void preparar() {
        service = new ManutencaoService(ativos, registros, estruturas, propriedades, catalogo,
                estoque, codigos, tarefas, alertas, preventiva,
                Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"), ZoneOffset.UTC));
        ativo = ativo(10L);
        lenient().when(ativos.findById(10L)).thenReturn(Optional.of(ativo));
        lenient().when(registros.saveAndFlush(any())).thenAnswer(inv -> {
            RegistroManutencao r = inv.getArgument(0); ReflectionTestUtils.setField(r, "id", 30L); return r;
        });
    }

    @Test void criaAtivoComCodigoAutomaticoEIdempotencia() {
        when(codigos.proximoAtivoPatrimonial()).thenReturn("PAT-2026-0001");
        when(ativos.saveAndFlush(any())).thenAnswer(inv -> { AtivoPatrimonial a=inv.getArgument(0); ReflectionTestUtils.setField(a,"id",10L); return a; });
        AtivoPatrimonialRequest request = ativoRequest("ativo-1");

        AtivoPatrimonialResumo criado = service.criarAtivo(request);

        assertThat(criado.codigo()).isEqualTo("PAT-2026-0001");
        verify(codigos).bloquearIdempotencia("PATRIMONIO_ATIVO", "ativo-1");
        verify(codigos).proximoAtivoPatrimonial();
    }

    @Test void registraManutencaoAgendaTarefaEAlerta() {
        RegistroManutencaoRequest request = manutencaoRequest("man-1");
        request.setProximaManutencao(LocalDateTime.of(2026, 10, 20, 8, 0));

        RegistroManutencaoResumo criado = service.registrarManutencao(request, operador);

        assertThat(criado.ativoCodigo()).isEqualTo("PAT-2026-0001");
        verify(tarefas).sincronizarAutomatica(argThat(t ->
                t.chaveAutomacao().equals(ManutencaoService.chaveTarefa(30L))
                        && t.moduloOrigem() == com.example.sitiopro.tarefas.entity.ModuloOrigem.MANUTENCAO), eq(operador));
        verify(alertas).avaliar();
    }

    @Test void multiplosConsumosUsamSomenteServicoOficial() {
        RegistroManutencaoRequest request = manutencaoRequest("man-consumo");
        request.setConsumos(List.of(consumo(40L, 50L, "1.2500"), consumo(41L, 50L, "2.0000")));
        ItemEstoque item1 = item(40L, "Óleo"); ItemEstoque item2 = item(41L, "Filtro");
        LocalEstoque local = local(50L);
        when(catalogo.buscarItem(40L)).thenReturn(item1); when(catalogo.buscarItem(41L)).thenReturn(item2);
        when(catalogo.buscarLocalAtivo(50L)).thenReturn(local);
        when(estoque.registrarConsumoManutencao(any(), eq(30L), eq(10L)))
                .thenReturn(movimento(60L), movimento(61L));

        RegistroManutencaoResumo criado = service.registrarManutencao(request, operador);

        assertThat(criado.consumos()).hasSize(2);
        verify(estoque, times(2)).registrarConsumoManutencao(any(), eq(30L), eq(10L));
    }

    @Test void retryNaoDuplicaMovimentoTarefaOuAlerta() {
        RegistroManutencao existente = registro(30L);
        when(registros.findByChaveIdempotencia("man-retry")).thenReturn(Optional.of(existente));

        service.registrarManutencao(manutencaoRequest("man-retry"), operador);

        verify(registros, never()).saveAndFlush(any());
        verifyNoInteractions(estoque, tarefas, alertas);
    }

    @Test void falhaDoEstoqueInterrompeFluxoAntesDeTarefaEAlerta() {
        RegistroManutencaoRequest request = manutencaoRequest("man-rollback");
        request.setConsumos(List.of(consumo(40L, 50L, "1")));
        when(catalogo.buscarItem(40L)).thenReturn(item(40L, "Peça"));
        when(catalogo.buscarLocalAtivo(50L)).thenReturn(local(50L));
        when(estoque.registrarConsumoManutencao(any(), eq(30L), eq(10L)))
                .thenThrow(new EstoqueOperacaoException("ESTOQUE_INSUFICIENTE", "Saldo insuficiente."));

        assertThatThrownBy(() -> service.registrarManutencao(request, operador)).hasMessage("Saldo insuficiente.");
        verifyNoInteractions(tarefas, alertas);
    }

    @Test void concluirProximaManutencaoFinalizaTarefaEResolveAlerta() {
        RegistroManutencao registro = registro(30L);
        registro.setProximaManutencao(LocalDateTime.of(2026, 10, 5, 8, 0));
        when(registros.buscarParaAtualizacao(30L)).thenReturn(Optional.of(registro));

        RegistroManutencaoResumo concluido = service.concluirProximaManutencao(30L, operador);

        assertThat(concluido.proximaManutencaoConcluida()).isTrue();
        verify(tarefas).concluirAutomatica(ManutencaoService.chaveTarefa(30L), operador);
        verify(alertas).avaliar();
    }

    @Test void validaDatasCustosEConsumoIncompleto() {
        RegistroManutencaoRequest request = manutencaoRequest("invalid");
        request.setCusto(new BigDecimal("-1"));
        assertThatThrownBy(() -> service.registrarManutencao(request, operador)).hasMessageContaining("custo");
        request.setCusto(BigDecimal.ZERO); request.setConsumos(List.of(consumo(40L, null, "1")));
        assertThatThrownBy(() -> service.registrarManutencao(request, operador)).hasMessageContaining("item, local");
    }

    @Test void manutencaoVinculadaConcluiCicloSemCriarAgendamentoManualDuplicado() {
        RegistroManutencaoRequest request = manutencaoRequest("man-plano"); request.setPlanoPreventivoId(20L);
        PlanoManutencaoPreventiva plano = new PlanoManutencaoPreventiva(); plano.setAtivoPatrimonial(ativo); plano.setNome("Troca de óleo");
        when(preventiva.buscarPlanoAtivoDoAtivo(20L, 10L)).thenReturn(plano);

        RegistroManutencaoResumo criado = service.registrarManutencao(request, operador);

        assertThat(criado.planoPreventivoNome()).isEqualTo("Troca de óleo");
        verify(preventiva).concluirCiclo(20L, 10L, request.getDataManutencao(), null, null, operador);
        verify(tarefas, never()).sincronizarAutomatica(any(), any());

        request.setProximaManutencao(LocalDateTime.of(2026, 11, 1, 8, 0));
        assertThatThrownBy(() -> service.registrarManutencao(request, operador)).hasMessageContaining("próximo ciclo");
    }

    private AtivoPatrimonialRequest ativoRequest(String chave) { AtivoPatrimonialRequest r=new AtivoPatrimonialRequest(); r.setNome("Motobomba"); r.setTipo(TipoAtivoPatrimonial.BOMBA); r.setStatus(StatusAtivoPatrimonial.ATIVO); r.setChaveIdempotencia(chave); return r; }
    private RegistroManutencaoRequest manutencaoRequest(String chave) { RegistroManutencaoRequest r=new RegistroManutencaoRequest(); r.setAtivoId(10L); r.setTipo(TipoManutencao.PREVENTIVA); r.setDataManutencao(LocalDateTime.of(2026,10,6,8,0)); r.setDescricao("Revisão geral"); r.setResponsavel("Operador"); r.setCusto(new BigDecimal("120.00")); r.setChaveIdempotencia(chave); return r; }
    private ConsumoManutencaoRequest consumo(Long item, Long local, String quantidade) { ConsumoManutencaoRequest c=new ConsumoManutencaoRequest(); c.setItemEstoqueId(item); c.setLocalEstoqueId(local); c.setQuantidade(new BigDecimal(quantidade)); return c; }
    private AtivoPatrimonial ativo(Long id) { AtivoPatrimonial a=new AtivoPatrimonial(); ReflectionTestUtils.setField(a,"id",id); a.setCodigo("PAT-2026-0001"); a.setNome("Motobomba"); a.setTipo(TipoAtivoPatrimonial.BOMBA); a.setStatus(StatusAtivoPatrimonial.ATIVO); return a; }
    private RegistroManutencao registro(Long id) { RegistroManutencao r=new RegistroManutencao(); ReflectionTestUtils.setField(r,"id",id); r.setAtivo(ativo); r.setTipo(TipoManutencao.PREVENTIVA); r.setDataManutencao(LocalDateTime.of(2026,10,6,8,0)); r.setDescricao("Revisão"); r.setResponsavel("Operador"); r.setCusto(BigDecimal.ZERO); return r; }
    private ItemEstoque item(Long id,String nome) { ItemEstoque i=new ItemEstoque(); ReflectionTestUtils.setField(i,"id",id); i.setNome(nome); UnidadeMedida u=new UnidadeMedida(); u.setSigla("UN"); i.setUnidadeMedida(u); return i; }
    private LocalEstoque local(Long id) { LocalEstoque l=new LocalEstoque(); ReflectionTestUtils.setField(l,"id",id); l.setNome("Oficina"); return l; }
    private MovimentoEstoque movimento(Long id) { MovimentoEstoque m=new MovimentoEstoque(); ReflectionTestUtils.setField(m,"id",id); return m; }
}
