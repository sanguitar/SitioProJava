package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.RegistroSanitarioSuinosRepository;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.tarefas.service.AlertaService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuinosSanidadeAlertasServiceTests {
    @Mock RegistroSanitarioSuinosRepository registros;
    @Mock AlertaService alertas;
    SuinosSanidadeAlertasService service;

    @BeforeEach void preparar() {
        service = new SuinosSanidadeAlertasService(registros, alertas,
                Clock.fixed(Instant.parse("2026-09-25T12:00:00Z"), ZoneOffset.UTC));
    }

    @Test void procedimentoVencidoGeraAlertaIdempotente() {
        when(registros.findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(LocalDate.of(2026, 9, 25)))
                .thenReturn(List.of(registro()));
        service.avaliar();
        verify(alertas).sincronizar(eq(ModuloOrigem.CRIACOES),
                eq(TipoAlerta.CRIACAO_SUINOS_PROCEDIMENTO_SANITARIO_VENCIDO),
                argThat(c -> c.size() == 1 && c.getFirst().chaveDeduplicacao()
                        .equals("CRIACAO:SUINOS:SANIDADE:30:VENCIDO")));
    }

    @Test void ausenciaDeVencimentoResolveAlertaAnterior() {
        when(registros.findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(any())).thenReturn(List.of());
        service.avaliar();
        verify(alertas).sincronizar(ModuloOrigem.CRIACOES,
                TipoAlerta.CRIACAO_SUINOS_PROCEDIMENTO_SANITARIO_VENCIDO, List.of());
    }

    private RegistroSanitarioSuinos registro() {
        LoteSuinos lote = new LoteSuinos(); ReflectionTestUtils.setField(lote, "id", 10L); lote.setCodigo("SU-2026-0010");
        RegistroSanitarioSuinos registro = new RegistroSanitarioSuinos(); ReflectionTestUtils.setField(registro, "id", 30L);
        registro.setLote(lote); registro.setProximaAcao("Reforço vacinal");
        registro.setProximaAcaoData(LocalDate.of(2026, 9, 24)); return registro;
    }
}
