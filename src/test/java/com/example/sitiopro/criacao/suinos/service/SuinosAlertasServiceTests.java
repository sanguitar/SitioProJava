package com.example.sitiopro.criacao.suinos.service;

import com.example.sitiopro.criacao.suinos.entity.*;
import com.example.sitiopro.criacao.suinos.repository.CicloReprodutivoSuinosRepository;
import com.example.sitiopro.tarefas.dto.CondicaoAlerta;
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
class SuinosAlertasServiceTests {
    @Mock CicloReprodutivoSuinosRepository ciclos;
    @Mock AlertaService alertas;
    SuinosAlertasService service;

    @BeforeEach void preparar() { service = new SuinosAlertasService(ciclos, alertas,
            Clock.fixed(Instant.parse("2026-09-24T12:00:00Z"), ZoneOffset.UTC)); }

    @Test void geraAlertasDeChecagemEPartoAtrasados() {
        when(ciclos.findByStatusAndDataPrevistaChecagemBefore(StatusCicloReprodutivoSuinos.AGUARDANDO_CHECAGEM, LocalDate.of(2026,9,25))).thenReturn(List.of(ciclo(1L)));
        when(ciclos.findByStatusAndDataPrevistaPartoBefore(StatusCicloReprodutivoSuinos.GESTANTE, LocalDate.of(2026,9,24))).thenReturn(List.of(ciclo(2L)));
        service.avaliar();
        verify(alertas).sincronizar(eq(ModuloOrigem.CRIACOES), eq(TipoAlerta.CRIACAO_SUINOS_CHECAGEM_PENDENTE), argThat(c -> chave(c).contains("CHECAGEM_PENDENTE")));
        verify(alertas).sincronizar(eq(ModuloOrigem.CRIACOES), eq(TipoAlerta.CRIACAO_SUINOS_PARTO_ATRASADO), argThat(c -> chave(c).contains("PARTO_ATRASADO")));
    }

    @Test void ausenciaDaCondicaoResolveAlertasPelaSincronizacao() {
        when(ciclos.findByStatusAndDataPrevistaChecagemBefore(any(), any())).thenReturn(List.of());
        when(ciclos.findByStatusAndDataPrevistaPartoBefore(any(), any())).thenReturn(List.of());
        service.avaliar();
        verify(alertas).sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_SUINOS_CHECAGEM_PENDENTE, List.of());
        verify(alertas).sincronizar(ModuloOrigem.CRIACOES, TipoAlerta.CRIACAO_SUINOS_PARTO_ATRASADO, List.of());
    }

    private String chave(List<CondicaoAlerta> condicoes) { return condicoes.getFirst().chaveDeduplicacao(); }
    private CicloReprodutivoSuinos ciclo(Long id) { var lote = new LoteSuinos(); ReflectionTestUtils.setField(lote,"id",10L); lote.setCodigo("SU-1"); var matriz = new AnimalReprodutivoSuinos(); ReflectionTestUtils.setField(matriz,"id",20L); matriz.setCodigo("SR-1"); matriz.setLote(lote); var ciclo = new CicloReprodutivoSuinos(); ReflectionTestUtils.setField(ciclo,"id",id); ciclo.setMatriz(matriz); ciclo.setDataPrevistaChecagem(LocalDate.of(2026,9,20)); ciclo.setDataPrevistaParto(LocalDate.of(2026,9,23)); return ciclo; }
}
