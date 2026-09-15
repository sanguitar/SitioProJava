package com.example.sitiopro.propriedade;

import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.propriedade.service.*;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PerimetroServiceTests {
    static ValidatorFactory factory;
    PropriedadeRepository propriedades = mock(PropriedadeRepository.class);
    PerimetroPropriedadeRepository perimetros = mock(PerimetroPropriedadeRepository.class);
    TalhaoRepository talhoes = mock(TalhaoRepository.class);
    PerimetroSpatialRepository spatial = mock(PerimetroSpatialRepository.class);
    PerimetroService service;
    @BeforeAll static void iniciar() { factory = Validation.buildDefaultValidatorFactory(); }
    @AfterAll static void fechar() { factory.close(); }
    @BeforeEach void dados() {
        service = new PerimetroService(propriedades, perimetros, talhoes, spatial, factory.getValidator());
        var p = new Propriedade(); org.springframework.test.util.ReflectionTestUtils.setField(p,"id",1L);
        when(propriedades.findByPrincipalTrue()).thenReturn(Optional.of(p));
        when(propriedades.bloquearPrincipal()).thenReturn(Optional.of(p));
        when(perimetros.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
    }
    static VerticePerimetroRequest vertice(int ordem, String lat, String lon) {
        var v = new VerticePerimetroRequest(); v.setOrdem(ordem); v.setLatitude(new BigDecimal(lat)); v.setLongitude(new BigDecimal(lon)); return v;
    }
    static PerimetroRequest request(VerticePerimetroRequest... vertices) {
        var r = new PerimetroRequest(); r.setVersao(-1L); r.setVertices(new ArrayList<>(List.of(vertices))); return r;
    }
    @Test void leituraVaziaNaoPersisteNemAssumeCrs() {
        var p = service.obter(); assertThat(p.statusCrs()).isEqualTo(StatusCrs.NAO_CONFIRMADO);
        assertThat(p.crs()).isNull(); assertThat(p.datum()).isNull(); assertThat(p.versao()).isEqualTo(-1);
        assertThat(p.getQuantidadeVertices()).isZero(); verify(perimetros,never()).saveAndFlush(any());
    }
    @Test void ordenaPorOrdemExplicitaSemRecalcularArea() {
        var r = request(vertice(3,"1","2"),vertice(1,"2","3"),vertice(2,"3","4"));
        var p = service.salvar(r);
        assertThat(p.vertices()).extracting(PerimetroResumo.Vertice::ordem).containsExactly(1,2,3);
        assertThat(p.getQuantidadeVertices()).isEqualTo(3);
        assertThat(p.getStatusGeorreferenciamento()).isEqualTo("CRS_NAO_CONFIRMADO");
        assertThat(r.getVertices().getFirst().getOrdem()).isEqualTo(3);
    }
    @Test void mapaOperacionalVazioNaoFechaPoligonoNemAssumeCrs() {
        var mapa = PerimetroResumo.vazio().getMapa();
        assertThat(mapa.formato()).isEqualTo("SITIOPRO_PERIMETRO_OPERACIONAL");
        assertThat(mapa.vertices()).isEmpty();
        assertThat(mapa.poligonoFechado()).isEmpty();
        assertThat(mapa.crsConfirmado()).isFalse();
        assertThat(mapa.aviso()).contains("Nao representa area juridica");
        assertThat(PerimetroResumo.vazio().getGeoJson().geometry()).isNull();
    }
    @ParameterizedTest @CsvSource({"1,0","2,0","3,4","4,5"})
    void mapaFechaVisualmenteSomenteComTresOuMaisVertices(int totalVertices, int pontosFechados) {
        var vertices = new VerticePerimetroRequest[totalVertices];
        for (int i = 0; i < totalVertices; i++) {
            vertices[i] = vertice(i + 1, String.valueOf(i + 1), String.valueOf(i + 2));
            vertices[i].setMarco("M" + (i + 1));
        }
        var mapa = service.salvar(request(vertices)).getMapa();
        assertThat(mapa.vertices()).extracting(PerimetroMapaResumo.Ponto::ordem)
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(1, totalVertices).boxed().toList());
        assertThat(mapa.poligonoFechado()).hasSize(pontosFechados);
        if (pontosFechados > 0) {
            assertThat(mapa.poligonoFechado().getLast()).usingRecursiveComparison().isEqualTo(mapa.vertices().getFirst());
        }
    }
    @Test void geoJsonPreservaOrdemUsaLongitudeLatitudeAltitudeEFechaPoligono() {
        var v1 = vertice(1,"-8.346821111","-63.871070000"); v1.setAltitudeGeodesicaM(new BigDecimal("90.30"));
        var v2 = vertice(2,"-8.350538889","-63.871139722"); v2.setAltitudeGeodesicaM(new BigDecimal("89.88"));
        var v3 = vertice(3,"-8.350611389","-63.871590833"); v3.setAltitudeGeodesicaM(new BigDecimal("88.97"));
        var r = request(v1, v2, v3);
        var geoJson = service.salvar(r).getGeoJson();
        assertThat(geoJson.type()).isEqualTo("Feature");
        assertThat(geoJson.geometry().type()).isEqualTo("Polygon");
        assertThat(geoJson.geometry().coordinates().toString()).contains("-63.871070000", "-8.346821111", "90.30");
        assertThat(geoJson.properties()).containsEntry("statusCrs", "NAO_CONFIRMADO");
    }
    @ParameterizedTest @CsvSource({"90,180","-90,-180","0,0","-8.1234567,-63.1234567"})
    void aceitaLimitesPrecisao(String lat,String lon) {
        assertThat(service.salvar(request(vertice(1,lat,lon))).vertices().getFirst().latitude()).isEqualByComparingTo(lat);
    }
    @ParameterizedTest @CsvSource({"90.0000001,0","-90.0000001,0","0,180.0000001","0,-180.0000001","1.1234567891,0"})
    void rejeitaCoordenadasInvalidas(String lat,String lon) {
        assertThatThrownBy(() -> service.salvar(request(vertice(1,lat,lon)))).isInstanceOf(PropriedadeOperacaoException.class);
        verify(perimetros,never()).saveAndFlush(any());
    }
    @Test void duplicataNumericaIndependeDaEscala() {
        assertThatThrownBy(() -> service.salvar(request(vertice(1,"1","2"),vertice(2,"1.00","2.000"))))
                .hasMessageContaining("duplicadas");
    }
    @Test void impedeOrdemDuplicada() {
        assertThatThrownBy(() -> service.salvar(request(vertice(1,"1","2"),vertice(1,"2","3"))))
                .hasMessageContaining("ordem");
    }
    @Test void exigeReferenciaAoConfirmarCrs() {
        var r = request(); r.setStatusCrs(StatusCrs.CONFIRMADO); r.setCrs("  ");
        assertThatThrownBy(() -> service.salvar(r)).hasMessageContaining("CRS");
        r.setCrs("Referencia documentada"); r.setDatum("Datum informado");
        assertThat(service.salvar(r).crs()).isEqualTo("Referencia documentada");
    }
    @Test void versaoAusenteOuObsoletaNaoSobrescreve() {
        var r = request(); r.setVersao(null);
        assertThatThrownBy(() -> service.salvar(r)).isInstanceOf(PropriedadeOperacaoException.class);
        when(perimetros.findByPropriedadeId(1L)).thenReturn(Optional.of(new PerimetroPropriedade()));
        r.setVersao(-1L);
        assertThatThrownBy(() -> service.salvar(r)).hasMessageContaining("Recarregue");
    }
    @Test void rejeitaVerticeNuloOuSemCoordenada() {
        var r = request(); r.getVertices().add(null);
        assertThatThrownBy(() -> service.salvar(r)).isInstanceOf(PropriedadeOperacaoException.class);
        r.getVertices().clear(); r.getVertices().add(new VerticePerimetroRequest());
        assertThatThrownBy(() -> service.salvar(r)).isInstanceOf(PropriedadeOperacaoException.class);
    }
    @Test void propriedadeInativaNaoRecebePerimetro() {
        propriedades.findByPrincipalTrue().orElseThrow().setAtivo(false);
        assertThatThrownBy(() -> service.salvar(request())).hasMessageContaining("Reative");
    }
}
