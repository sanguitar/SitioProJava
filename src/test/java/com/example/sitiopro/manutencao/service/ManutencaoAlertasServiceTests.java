package com.example.sitiopro.manutencao.service;

import com.example.sitiopro.manutencao.entity.*;
import com.example.sitiopro.manutencao.repository.*;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManutencaoAlertasServiceTests {
    @Mock RegistroManutencaoRepository registros; @Mock PlanoManutencaoPreventivaRepository planos;
    @Mock LeituraMedidorAtivoRepository leituras; @Mock AlertaService alertas;
    ManutencaoAlertasService service;
    @BeforeEach void preparar(){service=new ManutencaoAlertasService(registros,planos,leituras,alertas,Clock.fixed(Instant.parse("2026-10-06T12:00:00Z"),ZoneOffset.UTC));lenient().when(planos.findByAtivoTrueOrderByProximaDataAscNomeAsc()).thenReturn(List.of());}
    @Test void geraAlertaIdempotenteParaManutencaoVencida(){when(registros.findByProximaManutencaoConcluidaFalseAndProximaManutencaoBefore(any())).thenReturn(List.of(registro()));service.avaliar();verify(alertas).sincronizar(eq(ModuloOrigem.MANUTENCAO),eq(TipoAlerta.MANUTENCAO_VENCIDA),argThat(c->c.size()==1&&c.get(0).chaveDeduplicacao().equals("MANUTENCAO:REGISTRO:30:VENCIDA")));}
    @Test void ausenciaResolveAlertasAnteriores(){when(registros.findByProximaManutencaoConcluidaFalseAndProximaManutencaoBefore(any())).thenReturn(List.of());service.avaliar();verify(alertas).sincronizar(ModuloOrigem.MANUTENCAO,TipoAlerta.MANUTENCAO_VENCIDA,List.of());}
    @Test void planoPorMedidorVencidoGeraAlertaComChaveEstavel(){PlanoManutencaoPreventiva p=new PlanoManutencaoPreventiva();ReflectionTestUtils.setField(p,"id",40L);p.setAtivoPatrimonial(ativo());p.setNome("Troca de óleo");p.setTipoPeriodicidade(TipoPeriodicidadeManutencao.HORIMETRO);p.setProximoValor(new java.math.BigDecimal("100"));p.setAtivo(true);when(registros.findByProximaManutencaoConcluidaFalseAndProximaManutencaoBefore(any())).thenReturn(List.of());when(planos.findByAtivoTrueOrderByProximaDataAscNomeAsc()).thenReturn(List.of(p));LeituraMedidorAtivo l=new LeituraMedidorAtivo();l.setHorimetro(new java.math.BigDecimal("100"));when(leituras.findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(10L)).thenReturn(Optional.of(l));service.avaliar();verify(alertas).sincronizar(eq(ModuloOrigem.MANUTENCAO),eq(TipoAlerta.MANUTENCAO_VENCIDA),argThat(c->c.size()==1&&c.get(0).chaveDeduplicacao().equals("MANUTENCAO:PLANO:40:VENCIDA")));}
    private RegistroManutencao registro(){AtivoPatrimonial a=new AtivoPatrimonial();ReflectionTestUtils.setField(a,"id",10L);a.setCodigo("PAT-2026-0001");a.setNome("Motobomba");RegistroManutencao r=new RegistroManutencao();ReflectionTestUtils.setField(r,"id",30L);r.setAtivo(a);r.setProximaManutencao(LocalDateTime.of(2026,10,5,8,0));return r;}
    private AtivoPatrimonial ativo(){AtivoPatrimonial a=new AtivoPatrimonial();ReflectionTestUtils.setField(a,"id",10L);a.setCodigo("PAT-2026-0001");a.setNome("Motobomba");return a;}
}
