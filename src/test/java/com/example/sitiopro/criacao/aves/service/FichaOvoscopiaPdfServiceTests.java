package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.FichaOvoscopiaIncubacaoAves;
import com.example.sitiopro.criacao.aves.dto.OvoIncubacaoAvesResumo;
import com.example.sitiopro.criacao.aves.entity.AchadoOvoscopiaAves;
import com.example.sitiopro.criacao.aves.entity.MetodoIncubacaoAves;
import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FichaOvoscopiaPdfServiceTests {
    @Test
    void geraPdfValidoComPendenciasAnteriores() throws Exception {
        FichaOvoscopiaPdfService service = new FichaOvoscopiaPdfService();
        FichaOvoscopiaIncubacaoAves ficha = new FichaOvoscopiaIncubacaoAves(
                "Sítio Guaratinguetá", "INC-2026-0001", MetodoIncubacaoAves.CHOCADEIRA,
                LocalDate.of(2026, 9, 1), 7, LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 14),
                List.of(new OvoIncubacaoAvesResumo(1L, 1, "Ovo 01",
                        AchadoOvoscopiaAves.REAVALIAR, LocalDate.of(2026, 9, 7),
                        "Reavaliar", true)));

        byte[] pdf = service.gerar(ficha);

        assertThat(pdf).startsWith("%PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        try (var document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }
}

