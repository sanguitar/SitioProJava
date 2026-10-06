package com.example.sitiopro.criacao.peixes.service;

import com.example.sitiopro.criacao.peixes.entity.*;
import com.example.sitiopro.criacao.peixes.repository.RegistroSanitarioPeixesRepository;
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
class PeixesSanidadeAlertasServiceTests {
    @Mock RegistroSanitarioPeixesRepository registros;
    @Mock AlertaService alertas;
    PeixesSanidadeAlertasService service;

    @BeforeEach void preparar() {
        service = new PeixesSanidadeAlertasService(registros, alertas,
                Clock.fixed(Instant.parse("2026-09-30T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test void geraCondicaoIdempotenteParaAcaoVencida() {
        when(registros.findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(any()))
                .thenReturn(List.of(registro()));
        service.avaliar();
        verify(alertas).sincronizar(eq(ModuloOrigem.CRIACOES),
                eq(TipoAlerta.CRIACAO_PEIXES_ACAO_SANITARIA_PENDENTE),
                argThat(c -> c.size() == 1
                        && c.get(0).chaveDeduplicacao().equals("CRIACAO:PEIXES:SANIDADE:30:PENDENTE")
                        && c.get(0).referenciaOrigem().equals(PeixesService.referencia(10L))));
    }

    @Test void ausenciaDePendenciasResolveAlertasAnteriores() {
        when(registros.findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(any()))
                .thenReturn(List.of());
        service.avaliar();
        verify(alertas).sincronizar(ModuloOrigem.CRIACOES,
                TipoAlerta.CRIACAO_PEIXES_ACAO_SANITARIA_PENDENTE, List.of());
    }

    private RegistroSanitarioPeixes registro() {
        LotePeixes lote = new LotePeixes(); ReflectionTestUtils.setField(lote, "id", 10L);
        lote.setCodigo("PX-2026-0001");
        RegistroSanitarioPeixes registro = new RegistroSanitarioPeixes();
        ReflectionTestUtils.setField(registro, "id", 30L); registro.setLote(lote);
        registro.setProximaAcao("Inspecionar");
        registro.setProximaAcaoData(LocalDateTime.of(2026, 9, 29, 8, 0));
        return registro;
    }
}
