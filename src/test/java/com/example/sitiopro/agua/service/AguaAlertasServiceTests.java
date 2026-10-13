package com.example.sitiopro.agua.service;

import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AguaAlertasServiceTests {
    @Mock RecursoHidricoRepository recursos;
    @Mock PropriedadeRepository propriedades;
    @Mock AlertaService alertas;
    AguaAlertasService service;

    @BeforeEach
    void preparar() {
        service = new AguaAlertasService(recursos, propriedades, alertas,
                Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC));
        Propriedade p = new Propriedade(); ReflectionTestUtils.setField(p, "id", 1L);
        when(propriedades.findByPrincipalTrue()).thenReturn(Optional.of(p));
    }

    @Test
    void geraEPermiteResolverAlertasIdempotentesDeNivelEInspecao() {
        RecursoHidrico r = new RecursoHidrico(); ReflectionTestUtils.setField(r, "id", 10L);
        r.setNome("Caixa"); r.setAtivo(true); r.setCapacidadeLitros(new BigDecimal("1000"));
        r.setVolumeAtualLitros(new BigDecimal("150"));
        r.setProximaInspecao(LocalDateTime.of(2026, 10, 7, 8, 0));
        when(recursos.findByPropriedadeIdAndAtivoTrueOrderByNomeAsc(1L)).thenReturn(List.of(r));

        service.avaliar();

        @SuppressWarnings("unchecked") ArgumentCaptor<List<CondicaoAlerta>> captor = ArgumentCaptor.forClass(List.class);
        verify(alertas).sincronizar(eq(ModuloOrigem.AGUA), eq(TipoAlerta.AGUA_NIVEL_BAIXO), captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(c -> {
            assertThat(c.chaveDeduplicacao()).isEqualTo("AGUA:RECURSO:10:NIVEL_BAIXO");
            assertThat(c.contexto()).containsEntry("limiteLitros", new BigDecimal("200.00"));
        });
        verify(alertas).sincronizar(eq(ModuloOrigem.AGUA), eq(TipoAlerta.AGUA_INSPECAO_VENCIDA),
                argThat(lista -> lista.size() == 1));

        r.setVolumeAtualLitros(new BigDecimal("500"));
        r.setProximaInspecao(LocalDateTime.of(2026, 10, 20, 8, 0));
        service.avaliar();
        verify(alertas).sincronizar(ModuloOrigem.AGUA, TipoAlerta.AGUA_NIVEL_BAIXO, List.of());
        verify(alertas).sincronizar(ModuloOrigem.AGUA, TipoAlerta.AGUA_INSPECAO_VENCIDA, List.of());
    }
}
