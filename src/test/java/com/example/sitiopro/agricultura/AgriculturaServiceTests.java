package com.example.sitiopro.agricultura;

import com.example.sitiopro.agricultura.dto.*;
import com.example.sitiopro.agricultura.entity.*;
import com.example.sitiopro.agricultura.repository.*;
import com.example.sitiopro.agricultura.service.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.repository.TalhaoRepository;
import com.example.sitiopro.propriedade.service.PerimetroService;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;
import com.example.sitiopro.integracao.clima.service.ClimaConsultaService;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.estoque.service.EstoqueMovimentoService;
import com.example.sitiopro.estoque.dto.*;
import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import com.example.sitiopro.tarefas.service.*;
import com.example.sitiopro.tarefas.dto.*;
import com.example.sitiopro.tarefas.entity.*;
import com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.validation.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class AgriculturaServiceTests {
    private static final ValidatorFactory VALIDATION = Validation.buildDefaultValidatorFactory();
    private static final LocalDate DATA = LocalDate.of(2026, 8, 1);
    SafraRepository safras = mock(SafraRepository.class);
    CulturaAgricolaRepository culturas = mock(CulturaAgricolaRepository.class);
    CultivoRepository cultivos = mock(CultivoRepository.class);
    PlantioRepository plantios = mock(PlantioRepository.class);
    AcompanhamentoCultivoRepository acompanhamentos = mock(AcompanhamentoCultivoRepository.class);
    ColheitaRepository colheitas = mock(ColheitaRepository.class);
    AdubacaoCultivoRepository adubacoes = mock(AdubacaoCultivoRepository.class);
    IrrigacaoCultivoRepository irrigacoes = mock(IrrigacaoCultivoRepository.class);
    TratamentoAgricolaRepository tratamentos = mock(TratamentoAgricolaRepository.class);
    OcorrenciaCultivoRepository ocorrencias = mock(OcorrenciaCultivoRepository.class);
    HistoricoOcorrenciaCultivoRepository historicosOcorrencia = mock(HistoricoOcorrenciaCultivoRepository.class);
    TalhaoRepository talhoes = mock(TalhaoRepository.class);
    AgrofitCulturaRepository agrofit = mock(AgrofitCulturaRepository.class);
    PerimetroService perimetros = mock(PerimetroService.class);
    PropriedadeService propriedades = mock(PropriedadeService.class);
    EstoqueMovimentoService estoque = mock(EstoqueMovimentoService.class);
    TarefaService tarefas = mock(TarefaService.class);
    AlertaService alertas = mock(AlertaService.class);
    EntityManager em = mock(EntityManager.class);
    AgriculturaService service;
    Propriedade propriedade; Safra safra; Talhao talhao; CulturaAgricola cultura; Cultivo cultivo;

    @BeforeEach void dados() {
        service = new AgriculturaService(safras, culturas, cultivos, plantios, acompanhamentos, colheitas,
                adubacoes, irrigacoes, tratamentos, ocorrencias, historicosOcorrencia,
                talhoes, agrofit, perimetros, propriedades, estoque, tarefas, alertas, ConfiguracaoOperacionalTestFixture.servico(),
                em, VALIDATION.getValidator(), Clock.fixed(Instant.parse("2026-09-03T12:00:00Z"), ZoneOffset.UTC));
        propriedade = id(new Propriedade(), 1); propriedade.setNome("Sitio teste");
        safra = id(new Safra(), 2); safra.setPropriedade(propriedade); safra.setNome("Safra teste");
        safra.setAnoInicio(2026); safra.setAnoFim(2026); safra.setDataInicio(LocalDate.of(2026,1,1));
        safra.setStatus(StatusSafra.EM_ANDAMENTO);
        talhao = id(new Talhao(), 3); talhao.setPropriedade(propriedade); talhao.setNome("Talhao oficial"); talhao.setAreaHa(new BigDecimal("2"));
        cultura = id(new CulturaAgricola(), 4); cultura.setNomeComum("Milho"); cultura.setCicloDiasEstimado(100);
        cultivo = id(new Cultivo(), 5); cultivo.setPropriedade(propriedade); cultivo.setSafra(safra);
        cultivo.setTalhao(talhao); cultivo.setCultura(cultura); cultivo.setAreaCultivadaHa(BigDecimal.ONE);
        cultivo.setDataPlantio(DATA); cultivo.setPrevisaoColheita(DATA.plusDays(100));
        when(propriedades.principal()).thenReturn(propriedade);
        when(safras.findByIdAndPropriedadeId(2L,1L)).thenReturn(Optional.of(safra));
        when(culturas.findById(4L)).thenReturn(Optional.of(cultura));
        when(talhoes.findByIdAndPropriedadeId(3L,1L)).thenReturn(Optional.of(talhao));
        when(cultivos.findByIdAndPropriedadeId(5L,1L)).thenReturn(Optional.of(cultivo));
        when(cultivos.areaReservada(anyLong(),any(),anyLong())).thenReturn(BigDecimal.ZERO);
        when(cultivos.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),5));
        when(safras.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),2));
        when(culturas.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),4));
        when(plantios.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),6));
        when(acompanhamentos.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),7));
        when(colheitas.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),8));
        when(adubacoes.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),9));
        when(irrigacoes.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),10));
        when(tratamentos.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),11));
        when(ocorrencias.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),12));
        when(historicosOcorrencia.saveAndFlush(any())).thenAnswer(i -> id(i.getArgument(0),13));
    }
    @AfterAll static void fecharValidator() { VALIDATION.close(); }
    static <T> T id(T entity, long id) { ReflectionTestUtils.setField(entity,"id",id); return entity; }
    CultivoRequest cultivoRequest() {
        var r = new CultivoRequest(); r.setSafraId(2L); r.setTalhaoId(3L); r.setCulturaId(4L);
        r.setAreaCultivadaHa(BigDecimal.ONE); r.setDataPlantio(DATA); r.setVersao(0L); return r;
    }
    SafraRequest safraRequest() {
        var r = new SafraRequest(); r.setNome("Safra 2026"); r.setAnoInicio(2026); r.setAnoFim(2026);
        r.setDataInicio(DATA); r.setVersao(0L); return r;
    }
    PlantioRequest plantio() {
        var r = new PlantioRequest(); r.setData(DATA); r.setQuantidade(BigDecimal.TEN); r.setUnidade("kg");
        r.setDescricaoOrigem("Fornecedor externo"); r.setVersao(0L); return r;
    }
    ColheitaRequest colheita() {
        var r = new ColheitaRequest(); r.setData(DATA.plusDays(20)); r.setQuantidade(BigDecimal.TEN);
        r.setUnidade("kg"); r.setVersao(0L); return r;
    }
    OcorrenciaCultivo ocorrenciaAberta() {
        OcorrenciaCultivo o = id(new OcorrenciaCultivo(), 12); o.setCultivo(cultivo);
        o.setDataHora(DATA.plusDays(5).atTime(9,0)); o.setTipo(TipoOcorrenciaCultivo.PRAGA);
        o.setTitulo("Lagartas nas folhas"); o.setDescricao("Focos observados no talhao");
        o.setSeveridade(SeveridadeOcorrencia.ALTA); o.setChaveIdempotencia("ocorrencia-base");
        return o;
    }
    AcompanhamentoRequest acompanhamento() {
        var r = new AcompanhamentoRequest(); r.setDataHora(DATA.plusDays(5).atTime(10,0));
        r.setDescricao("Desenvolvimento registrado em campo."); r.setVersao(0L); return r;
    }

    @Test void defaultsSeguros() {
        assertThat(new SafraRequest().getStatus()).isEqualTo(StatusSafra.PLANEJADA);
        assertThat(new CulturaRequest().isAtivo()).isTrue();
        assertThat(new PlantioRequest().getOrigem()).isEqualTo(OrigemPlantio.EXTERNA);
        assertThat(new ColheitaRequest().isFinalizaCultivo()).isTrue();
        assertThat(new ColheitaRequest().getDestino()).isEqualTo(DestinoColheita.SEM_ESTOQUE);
    }
    @Test void criaSafraDaPropriedadePrincipal() {
        var r = service.salvarSafra(null, safraRequest());
        assertThat(r.propriedadeId()).isEqualTo(1); assertThat(r.status()).isEqualTo(StatusSafra.PLANEJADA);
        verify(em).refresh(propriedade, LockModeType.PESSIMISTIC_WRITE);
    }
    @ParameterizedTest @ValueSource(ints={1899,10000})
    void rejeitaAnoInvalido(int ano) {
        var r = safraRequest(); r.setAnoInicio(ano);
        assertThatThrownBy(() -> service.salvarSafra(null,r)).isInstanceOf(ConstraintViolationException.class);
    }
    @Test void rejeitaAnosInvertidos() {
        var r = safraRequest(); r.setAnoFim(2025);
        assertThatThrownBy(() -> service.salvarSafra(null,r)).hasMessageContaining("intervalo");
    }
    @Test void rejeitaDataFimAnterior() {
        var r = safraRequest(); r.setDataFim(DATA.minusDays(1));
        assertThatThrownBy(() -> service.salvarSafra(null,r)).hasMessageContaining("intervalo");
    }
    @Test void rejeitaSafraDuplicada() {
        when(safras.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(anyLong(),anyString(),anyLong())).thenReturn(true);
        assertThatThrownBy(() -> service.salvarSafra(null,safraRequest())).hasMessageContaining("Ja existe");
    }
    @ParameterizedTest @EnumSource(value=StatusSafra.class,names={"ENCERRADA","CANCELADA"})
    void naoFechaSafraComCultivosAbertos(StatusSafra destino) {
        when(cultivos.existsBySafraIdAndStatusIn(eq(2L),any())).thenReturn(true);
        var r=safraRequest(); r.setStatus(destino);
        assertThatThrownBy(() -> service.salvarSafra(2L,r)).hasMessageContaining("Finalize os cultivos");
    }
    @Test void periodoDaSafraPreservaHistorico() {
        when(cultivos.existsForaDoPeriodo(anyLong(),any(),any())).thenReturn(true);
        assertThatThrownBy(() -> service.salvarSafra(2L,safraRequest())).hasMessageContaining("excluir datas");
    }
    @Test void culturaFuncionaSemAgrofit() {
        var r = new CulturaRequest(); r.setNomeComum("Feijao"); r.setCicloDiasEstimado(90);
        assertThat(service.salvarCultura(null,r).agrofitCulturaId()).isNull(); verifyNoInteractions(agrofit);
    }
    @Test void culturaValidaReferenciaAgrofitLocal() {
        var r = new CulturaRequest(); r.setNomeComum("Feijao"); r.setAgrofitCulturaId(999L);
        assertThatThrownBy(() -> service.salvarCultura(null,r)).hasMessageContaining("Referencia Agrofit");
    }
    @ParameterizedTest @ValueSource(ints={0,-1,3651})
    void culturaRejeitaCicloInvalido(int dias) {
        var r = new CulturaRequest(); r.setNomeComum("Teste"); r.setCicloDiasEstimado(dias);
        assertThatThrownBy(() -> service.salvarCultura(null,r)).isInstanceOf(ConstraintViolationException.class);
    }
    @Test void criaCultivoPlanejadoComPrevisaoCalculada() {
        var r = service.salvarCultivo(null,cultivoRequest());
        assertThat(r.status()).isEqualTo(StatusCultivo.PLANEJADO); assertThat(r.talhaoId()).isEqualTo(3);
        assertThat(r.previsaoColheita()).isEqualTo(DATA.plusDays(100)); assertThat(r.diasDesdePlantio()).isNull();
    }
    @Test void mapaOperacionalReusaGeometriaDoTalhaoEAgregaCultivoOcorrencias() {
        cultivo.setStatus(StatusCultivo.EM_DESENVOLVIMENTO);
        var talhaoMapa = TalhaoMapaResumo.de(3L, "TL-0003", "Talhao oficial", new BigDecimal("2.0000"),
                new BigDecimal("19990.1200"), true, List.of());
        when(perimetros.obter()).thenReturn(new PerimetroResumo(20L, 0, StatusCrs.CONFIRMADO, "EPSG:4674",
                "SIRGAS 2000", null, List.of(), null, null, PerimetroConferenciaResumo.vazio(),
                List.of(talhaoMapa)));
        when(cultivos.findByPropriedadeIdAndStatusInOrderByDataPlantioDescIdDesc(eq(1L), any())).thenReturn(List.of(cultivo));
        var media = ocorrenciaAberta(); media.setSeveridade(SeveridadeOcorrencia.MEDIA);
        var critica = ocorrenciaAberta(); critica.setSeveridade(SeveridadeOcorrencia.CRITICA);
        when(ocorrencias.findByCultivoPropriedadeIdAndStatusIn(eq(1L), any())).thenReturn(List.of(media, critica));

        var mapa = service.mapaOperacional();

        assertThat(mapa.crs()).isEqualTo("EPSG:4674");
        assertThat(mapa.talhoes()).hasSize(1);
        var talhaoOperacional = mapa.talhoes().getFirst();
        assertThat(talhaoOperacional.id()).isEqualTo(3L);
        assertThat(talhaoOperacional.areaGisM2()).isEqualByComparingTo("19990.1200");
        assertThat(talhaoOperacional.cultivoAtivo().id()).isEqualTo(5L);
        assertThat(talhaoOperacional.cultivoAtivo().cultura()).isEqualTo("Milho");
        assertThat(talhaoOperacional.cultivoAtivo().safra()).isEqualTo("Safra teste");
        assertThat(talhaoOperacional.cultivoAtivo().ocorrenciasAbertas()).isEqualTo(2);
        assertThat(talhaoOperacional.cultivoAtivo().severidadeMaisAlta()).isEqualTo(SeveridadeOcorrencia.CRITICA);
        assertThat(talhaoOperacional.possuiOcorrenciaRelevante()).isTrue();
    }
    @Test void culturaSemCicloNaoInventaPrevisao() {
        cultura.setCicloDiasEstimado(null);
        assertThat(service.salvarCultivo(null,cultivoRequest()).previsaoColheita()).isNull();
    }
    @ParameterizedTest @ValueSource(strings={"0","-1","2.0001","0.00001"})
    void rejeitaAreaInvalidaOuExcedente(String valor) {
        var r = cultivoRequest(); r.setAreaCultivadaHa(new BigDecimal(valor));
        assertThatThrownBy(() -> service.salvarCultivo(null,r)).isInstanceOfAny(ConstraintViolationException.class,AgriculturaOperacaoException.class);
    }
    @Test void rejeitaSuperposicaoFisica() {
        when(cultivos.areaReservada(anyLong(),any(),anyLong())).thenReturn(new BigDecimal("1.5"));
        assertThatThrownBy(() -> service.salvarCultivo(null,cultivoRequest())).hasMessageContaining("excede");
        verify(cultivos,never()).saveAndFlush(any());
    }
    @Test void exigeTalhaoOficial() {
        var r = cultivoRequest(); r.setTalhaoId(null);
        assertThatThrownBy(() -> service.salvarCultivo(null,r)).isInstanceOf(ConstraintViolationException.class);
    }
    @Test void rejeitaTalhaoDeOutraPropriedade() {
        var r = cultivoRequest(); r.setTalhaoId(99L);
        assertThatThrownBy(() -> service.salvarCultivo(null,r)).hasMessageContaining("Talhao oficial");
    }
    @Test void rejeitaTalhaoInativo() {
        talhao.setStatus(StatusDivisaoFisica.INATIVO);
        assertThatThrownBy(() -> service.salvarCultivo(null,cultivoRequest())).hasMessageContaining("ativos");
    }
    @Test void rejeitaCulturaInativa() {
        cultura.setAtivo(false);
        assertThatThrownBy(() -> service.salvarCultivo(null,cultivoRequest())).hasMessageContaining("ativos");
    }
    @Test void rejeitaSafraFinalizada() {
        safra.setStatus(StatusSafra.ENCERRADA);
        assertThatThrownBy(() -> service.salvarCultivo(null,cultivoRequest())).hasMessageContaining("safra aberta");
    }
    @Test void rejeitaPrevisaoAnteriorAoPlantio() {
        var r = cultivoRequest(); r.setPrevisaoColheita(DATA.minusDays(1));
        assertThatThrownBy(() -> service.salvarCultivo(null,r)).hasMessageContaining("Previsao");
    }
    @Test void rejeitaPlantioForaDaSafra() {
        var r = cultivoRequest(); r.setDataPlantio(LocalDate.of(2025,12,31));
        assertThatThrownBy(() -> service.salvarCultivo(null,r)).hasMessageContaining("periodo");
    }
    @Test void edicaoObsoletaRejeitada() {
        var r = cultivoRequest(); r.setVersao(8L);
        assertThatThrownBy(() -> service.salvarCultivo(5L,r)).hasMessageContaining("Recarregue");
    }
    @Test void naoReescreveDadosFisicosAposPlantio() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r = cultivoRequest(); r.setAreaCultivadaHa(new BigDecimal("1.2"));
        assertThatThrownBy(() -> service.salvarCultivo(5L,r)).hasMessageContaining("preserve");
    }
    @Test void plantaOrigemExternaSemEstoque() {
        var r = service.registrarPlantio(5L,plantio());
        assertThat(r.origem()).isEqualTo(OrigemPlantio.EXTERNA); assertThat(r.descricaoOrigem()).isEqualTo("Fornecedor externo");
        assertThat(cultivo.getStatus()).isEqualTo(StatusCultivo.IMPLANTADO);
        assertThat(cultivo.getRevisaoOperacoes()).isEqualTo(1); verifyNoInteractions(estoque);
    }
    @Test void dataRealRecalculaPrevisaoAutomatica() {
        var r=plantio(); r.setData(DATA.plusDays(2)); service.registrarPlantio(5L,r);
        assertThat(cultivo.getPrevisaoColheita()).isEqualTo(DATA.plusDays(102));
    }
    @Test void origemExternaDeveSerDescrita() {
        var r=plantio(); r.setDescricaoOrigem(" ");
        assertThatThrownBy(() -> service.registrarPlantio(5L,r)).hasMessageContaining("origem externa");
    }
    @Test void origemExternaNaoAceitaEstoqueOculto() {
        var r=plantio(); r.setItemEstoqueId(10L);
        assertThatThrownBy(() -> service.registrarPlantio(5L,r)).hasMessageContaining("nao deve");
    }
    @Test void consumoUtilizaServicoOficial() {
        var item = new ItemEstoqueResumo(10L,"Sementes","Insumos","kg",BigDecimal.TEN,null,true,false,null,null);
        when(estoque.detalharItem(10L)).thenReturn(new ItemEstoqueDetalhe(item,null,false,false,List.of(),List.of()));
        when(estoque.registrarConsumoAgricultura(any(),eq(5L))).thenReturn(id(new MovimentoEstoque(),11));
        var r=plantio(); r.setOrigem(OrigemPlantio.ESTOQUE); r.setItemEstoqueId(10L); r.setLocalEstoqueId(12L);
        assertThat(service.registrarPlantio(5L,r).movimentoEstoqueId()).isEqualTo(11);
        verify(estoque).registrarConsumoAgricultura(argThat(m -> m.getQuantidade().equals(BigDecimal.TEN)
                && m.getLocalOrigemId().equals(12L) && m.getItemId().equals(10L)),eq(5L));
    }
    @Test void consumoRejeitaUnidadeDivergente() {
        var item = new ItemEstoqueResumo(10L,"Sementes","Insumos","un",BigDecimal.TEN,null,true,false,null,null);
        when(estoque.detalharItem(10L)).thenReturn(new ItemEstoqueDetalhe(item,null,false,false,List.of(),List.of()));
        var r=plantio(); r.setOrigem(OrigemPlantio.ESTOQUE); r.setItemEstoqueId(10L); r.setLocalEstoqueId(12L);
        assertThatThrownBy(() -> service.registrarPlantio(5L,r)).hasMessageContaining("unidade");
        verify(estoque,never()).registrarConsumoAgricultura(any(),anyLong());
    }
    @Test void naoAceitaPlantioFuturo() {
        var r=plantio(); r.setData(LocalDate.of(2026,9,4));
        assertThatThrownBy(() -> service.registrarPlantio(5L,r)).hasMessageContaining("futura");
    }
    @ParameterizedTest @EnumSource(TipoAcompanhamentoCultivo.class)
    void registraTodosTiposDeAcompanhamento(TipoAcompanhamentoCultivo tipo) {
        var r=acompanhamento(); r.setTipo(tipo);
        assertThat(service.registrarAcompanhamento(5L,r).tipo()).isEqualTo(tipo);
    }
    @Test void acompanhamentoNaoPodeSerFuturo() {
        var r=acompanhamento(); r.setDataHora(LocalDateTime.of(2026,9,3,12,1));
        assertThatThrownBy(() -> service.registrarAcompanhamento(5L,r)).hasMessageContaining("futura");
    }
    @ParameterizedTest @EnumSource(value=StatusCultivo.class,names={"COLHIDO","CANCELADO","PERDIDO"})
    void cultivoFinalizadoNaoAceitaNovaOperacao(StatusCultivo status) {
        cultivo.setStatus(status);
        assertThatThrownBy(() -> service.registrarAcompanhamento(5L,acompanhamento())).hasMessageContaining("finalizado");
    }
    @Test void retryComVersaoAntigaNaoCriaOutroRegistro() {
        ReflectionTestUtils.setField(cultivo,"versao",1L);
        assertThatThrownBy(() -> service.registrarColheita(5L,colheita())).hasMessageContaining("Recarregue");
        verify(colheitas,never()).saveAndFlush(any());
    }
    @Test void colheitaExigePlantio() {
        assertThatThrownBy(() -> service.registrarColheita(5L,colheita())).hasMessageContaining("Registre o plantio");
    }
    @Test void colheitaDeveSerPosteriorAoUltimoReplantio() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        when(plantios.existsByCultivoIdAndDataAfter(eq(5L),any())).thenReturn(true);
        assertThatThrownBy(() -> service.registrarColheita(5L,colheita())).hasMessageContaining("ultimo plantio");
    }
    @Test void colheitaParcialMantemCultivoAberto() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=colheita(); r.setFinalizaCultivo(false); service.registrarColheita(5L,r);
        assertThat(cultivo.getStatus()).isEqualTo(StatusCultivo.PRONTO_COLHEITA); assertThat(cultivo.getDataColheitaReal()).isNull();
    }
    @Test void colheitaFinalLiberaCultivoSemEntradaAutomaticaEstoque() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=service.registrarColheita(5L,colheita());
        assertThat(r.quantidade()).isEqualByComparingTo("10"); assertThat(cultivo.getStatus()).isEqualTo(StatusCultivo.COLHIDO);
        assertThat(cultivo.getDataColheitaReal()).isEqualTo(DATA.plusDays(20)); verifyNoInteractions(estoque);
    }
    @Test void colheitaPreservaUnidadeEntreParciais() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var anterior = new Colheita(); anterior.setUnidade("caixa"); anterior.setData(DATA.plusDays(10));
        when(colheitas.findFirstByCultivoIdOrderByDataDescIdDesc(5L)).thenReturn(Optional.of(anterior));
        assertThatThrownBy(() -> service.registrarColheita(5L,colheita())).hasMessageContaining("unidade");
    }
    @Test void colheitaRejeitaPerdasNegativas() {
        var r=colheita(); r.setPerdas(BigDecimal.ONE.negate());
        assertThatThrownBy(() -> service.registrarColheita(5L,r)).isInstanceOf(ConstraintViolationException.class);
    }
    @Test void registraAdubacaoExternaAuditavel() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=new AdubacaoRequest(); r.setData(DATA.plusDays(2)); r.setProduto("Composto");
        r.setQuantidade(new BigDecimal("4.5000")); r.setUnidade("kg"); r.setAreaAplicadaHa(new BigDecimal("0.5"));
        r.setDescricaoOrigem("Producao local"); r.setChaveIdempotencia("adubacao-001"); r.setVersao(0L);
        var salvo=service.registrarAdubacao(5L,r);
        assertThat(salvo.produto()).isEqualTo("Composto"); assertThat(salvo.movimentoEstoqueId()).isNull();
        verifyNoInteractions(estoque);
    }
    @Test void retryDeAdubacaoRetornaMesmoRegistroSemNovoSaldo() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=new AdubacaoRequest(); r.setData(DATA.plusDays(2)); r.setProduto("Composto");
        r.setQuantidade(BigDecimal.ONE); r.setUnidade("kg"); r.setDescricaoOrigem("Externa");
        r.setChaveIdempotencia("adubacao-retry"); r.setVersao(0L);
        service.registrarAdubacao(5L,r);
        var captor=org.mockito.ArgumentCaptor.forClass(AdubacaoCultivo.class); verify(adubacoes).saveAndFlush(captor.capture());
        when(adubacoes.findByCultivoIdAndChaveIdempotencia(5L,"adubacao-retry")).thenReturn(Optional.of(captor.getValue()));
        assertThat(service.registrarAdubacao(5L,r).id()).isEqualTo(9L);
        verify(adubacoes,times(1)).saveAndFlush(any()); verifyNoInteractions(estoque);
    }
    @Test void irrigacaoExigeDuracaoOuVolume() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=new IrrigacaoRequest(); r.setDataHora(DATA.plusDays(3).atTime(8,0));
        r.setChaveIdempotencia("irrigacao-001"); r.setVersao(0L);
        assertThatThrownBy(() -> service.registrarIrrigacao(5L,r)).hasMessageContaining("duracao ou o volume");
    }
    @Test void registraIrrigacaoComBigDecimal() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=new IrrigacaoRequest(); r.setDataHora(DATA.plusDays(3).atTime(8,0)); r.setDuracaoMinutos(45);
        r.setVolumeLitros(new BigDecimal("1250.1250")); r.setChaveIdempotencia("irrigacao-002"); r.setVersao(0L);
        assertThat(service.registrarIrrigacao(5L,r).volumeLitros()).isEqualByComparingTo("1250.1250");
    }
    @Test void tratamentoComEstoqueUsaMovimentoOficial() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var item=new ItemEstoqueResumo(10L,"Produto oficial","Insumos","L",BigDecimal.TEN,null,true,false,null,null);
        when(estoque.detalharItem(10L)).thenReturn(new ItemEstoqueDetalhe(item,null,false,false,List.of(),List.of()));
        when(estoque.registrarConsumoAgriculturaOperacao(any(),eq(5L),eq("Tratamento agricola")))
                .thenReturn(id(new MovimentoEstoque(),21));
        var r=new TratamentoRequest(); r.setData(DATA.plusDays(4)); r.setFinalidade("Controle observado");
        r.setProdutoAplicado("Produto informado"); r.setQuantidade(BigDecimal.ONE); r.setUnidade("L");
        r.setOrigem(OrigemInsumo.ESTOQUE); r.setItemEstoqueId(10L); r.setLocalEstoqueId(12L);
        r.setChaveIdempotencia("tratamento-001"); r.setVersao(0L);
        var salvo=service.registrarTratamento(5L,r);
        assertThat(salvo.produtoAplicado()).isEqualTo("Produto oficial"); assertThat(salvo.movimentoEstoqueId()).isEqualTo(21L);
    }
    @Test void ocorrenciaParcialMantemCultivoEPerdaTotalEncerra() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=new OcorrenciaRequest(); r.setDataHora(DATA.plusDays(5).atTime(9,0)); r.setTipo(TipoOcorrenciaCultivo.DANO_CLIMATICO);
        r.setSeveridade(SeveridadeOcorrencia.ALTA); r.setTitulo("Dano por vento"); r.setDescricao("Dano observado");
        r.setQuantidadePerdida(BigDecimal.ONE); r.setUnidadePerda("kg"); r.setChaveIdempotencia("ocorrencia-001"); r.setVersao(0L);
        assertThat(service.registrarOcorrencia(5L,r).perdaTotal()).isFalse();
        ReflectionTestUtils.setField(cultivo,"versao",1L); r.setChaveIdempotencia("ocorrencia-002"); r.setVersao(1L); r.setPerdaTotal(true);
        assertThat(service.registrarOcorrencia(5L,r).perdaTotal()).isTrue();
        assertThat(cultivo.getStatus()).isEqualTo(StatusCultivo.PERDIDO);
    }
    @ParameterizedTest @EnumSource(SeveridadeOcorrencia.class)
    void ocorrenciaAceitaTodasAsSeveridades(SeveridadeOcorrencia severidade) {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r = new OcorrenciaRequest(); r.setDataHora(DATA.plusDays(5).atTime(9,0));
        r.setTipo(TipoOcorrenciaCultivo.PRAGA); r.setTitulo("Insetos observados");
        r.setDescricao("Registro de campo"); r.setSeveridade(severidade);
        r.setChaveIdempotencia("ocorrencia-severidade-" + severidade.name()); r.setVersao(0L);
        assertThat(service.registrarOcorrencia(5L,r).severidade()).isEqualTo(severidade);
    }
    @Test void ocorrenciaAltaAssociaAgrofitLocalEGeraAlertaIdempotente() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        AgrofitCultura referencia = id(new AgrofitCultura("Milho oficial", "MILHO", DATA.atStartOfDay()), 44);
        when(agrofit.findAllById(List.of(44L))).thenReturn(List.of(referencia));
        java.util.concurrent.atomic.AtomicReference<OcorrenciaCultivo> salva = new java.util.concurrent.atomic.AtomicReference<>();
        doAnswer(i -> {
            OcorrenciaCultivo o=id(i.getArgument(0,OcorrenciaCultivo.class),12); salva.set(o); return o;
        }).when(ocorrencias).saveAndFlush(any());
        when(ocorrencias.findByStatusInAndSeveridadeInOrderById(any(),any()))
                .thenAnswer(i -> salva.get() == null ? List.of() : List.of(salva.get()));
        var r = new OcorrenciaRequest(); r.setDataHora(DATA.plusDays(5).atTime(9,0));
        r.setTipo(TipoOcorrenciaCultivo.PRAGA); r.setTitulo("Lagarta no milho");
        r.setDescricao("Focos em folhas novas"); r.setSeveridade(SeveridadeOcorrencia.ALTA);
        r.setAgrofitCulturaIds(List.of(44L)); r.setChaveIdempotencia("ocorrencia-alerta-001"); r.setVersao(0L);
        OcorrenciaResumo resumo = service.registrarOcorrencia(5L,r);
        assertThat(resumo.referenciasAgrofit()).singleElement().extracting(ReferenciaAgrofitResumo::nome)
                .isEqualTo("Milho oficial");
        verify(historicosOcorrencia).saveAndFlush(argThat(h -> h.getTipo() == TipoHistoricoOcorrencia.REGISTRO));
        verify(alertas).sincronizar(eq(ModuloOrigem.AGRICULTURA),
                eq(TipoAlerta.AGRICULTURA_OCORRENCIA_FITOSSANITARIA),
                argThat(c -> c.size() == 1 && c.getFirst().chaveDeduplicacao().equals(
                        "AGRICULTURA:OCORRENCIA:12:FITOSSANITARIA")));
    }
    @Test void ocorrenciaFuncionaComCatalogoAgrofitDegradado() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r = new OcorrenciaRequest(); r.setDataHora(DATA.plusDays(5).atTime(9,0));
        r.setTipo(TipoOcorrenciaCultivo.DEFICIENCIA); r.setTitulo("Folhas amareladas");
        r.setDescricao("Sintoma ainda em avaliacao"); r.setSeveridade(SeveridadeOcorrencia.MEDIA);
        r.setChaveIdempotencia("ocorrencia-sem-agrofit"); r.setVersao(0L);
        assertThat(service.registrarOcorrencia(5L,r).referenciasAgrofit()).isEmpty();
        verify(agrofit, never()).findAllById(any());
    }
    @Test void encerramentoNovoUsaRelogioOperacional() {
        var r=service.novoEncerramentoOcorrencia(7L);
        assertThat(r.getDataHora()).isEqualTo(LocalDateTime.of(2026,9,3,12,0));
        assertThat(r.getVersao()).isEqualTo(7L);
    }
    @Test void atualizacaoRegistraHistoricoUmaVezEAtualizaAlerta() {
        OcorrenciaCultivo o = ocorrenciaAberta();
        when(ocorrencias.buscarParaAtualizacao(12L,1L)).thenReturn(Optional.of(o));
        when(ocorrencias.saveAndFlush(o)).thenReturn(o);
        when(ocorrencias.findByStatusInAndSeveridadeInOrderById(any(),any())).thenReturn(List.of(o));
        var r = new OcorrenciaAtualizacaoRequest(); r.setTipo(TipoOcorrenciaCultivo.DOENCA);
        r.setSeveridade(SeveridadeOcorrencia.CRITICA); r.setTitulo("Lesoes em expansao");
        r.setDescricao("Novos pontos encontrados"); r.setAcompanhamento("Area reavaliada em campo");
        r.setChaveIdempotencia("ocorrencia-atualiza-001"); r.setVersao(0L);
        assertThat(service.atualizarOcorrencia(12L,r).status()).isEqualTo(StatusOcorrenciaCultivo.EM_ACOMPANHAMENTO);
        HistoricoOcorrenciaCultivo salvo = id(new HistoricoOcorrenciaCultivo(),13);
        when(historicosOcorrencia.findByOcorrenciaIdAndChaveIdempotencia(12L,"ocorrencia-atualiza-001"))
                .thenReturn(Optional.of(salvo));
        assertThat(service.atualizarOcorrencia(12L,r).titulo()).isEqualTo("Lesoes em expansao");
        verify(ocorrencias, times(1)).saveAndFlush(o);
        verify(historicosOcorrencia, times(1)).saveAndFlush(any());
    }
    @Test void encerramentoResolveCondicaoDeAlertaEIdempotente() {
        OcorrenciaCultivo o = ocorrenciaAberta();
        when(ocorrencias.buscarParaAtualizacao(12L,1L)).thenReturn(Optional.of(o));
        when(ocorrencias.saveAndFlush(o)).thenReturn(o);
        when(ocorrencias.findByStatusInAndSeveridadeInOrderById(any(),any())).thenReturn(List.of());
        var r = new EncerramentoOcorrenciaRequest(); r.setDataHora(DATA.plusDays(6).atTime(10,0));
        r.setResolucao("Foco removido e area estabilizada"); r.setChaveIdempotencia("ocorrencia-encerra-001");
        r.setVersao(0L);
        assertThat(service.encerrarOcorrencia(12L,r).status()).isEqualTo(StatusOcorrenciaCultivo.ENCERRADA);
        verify(alertas).sincronizar(eq(ModuloOrigem.AGRICULTURA),
                eq(TipoAlerta.AGRICULTURA_OCORRENCIA_FITOSSANITARIA), eq(List.of()));
        when(historicosOcorrencia.findByOcorrenciaIdAndChaveIdempotencia(12L,"ocorrencia-encerra-001"))
                .thenReturn(Optional.of(id(new HistoricoOcorrenciaCultivo(),13)));
        service.encerrarOcorrencia(12L,r);
        verify(ocorrencias, times(1)).saveAndFlush(o);
    }
    @Test void tarefaDeInspecaoUsaChaveAutomaticaEstavel() {
        OcorrenciaCultivo o = ocorrenciaAberta();
        when(ocorrencias.buscarParaAtualizacao(12L,1L)).thenReturn(Optional.of(o));
        UsuarioAtor ator = new UsuarioAtor(7L,"operador",false);
        service.criarTarefaInspecao(12L,ator); service.criarTarefaInspecao(12L,ator);
        verify(tarefas, times(2)).sincronizarAutomatica(argThat(r ->
                r.chaveAutomacao().equals("AGRICULTURA:OCORRENCIA:12:INSPECAO")
                        && r.referenciaOrigem().equals("OCORRENCIA:12")
                        && r.prioridade() == PrioridadeTarefa.ALTA), eq(ator));
    }
    @Test void colheitaComEstoqueRegistraUmaEntradaOficial() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var item=new ItemEstoqueResumo(10L,"Milho colhido","Producao","kg",BigDecimal.ZERO,null,true,false,null,null);
        when(estoque.detalharItem(10L)).thenReturn(new ItemEstoqueDetalhe(item,null,false,false,List.of(),List.of()));
        when(estoque.registrarEntradaAgriculturaColheita(any(),eq(5L))).thenReturn(id(new MovimentoEstoque(),22));
        var r=colheita(); r.setDestino(DestinoColheita.ESTOQUE); r.setItemEstoqueId(10L); r.setLocalEstoqueId(12L);
        r.setChaveIdempotencia("colheita-estoque-001");
        var salvo=service.registrarColheita(5L,r);
        assertThat(salvo.destino()).isEqualTo(DestinoColheita.ESTOQUE); assertThat(salvo.movimentoEstoqueId()).isEqualTo(22L);
        verify(estoque).registrarEntradaAgriculturaColheita(argThat(m -> m.getLocalDestinoId().equals(12L)
                && m.getQuantidade().compareTo(BigDecimal.TEN)==0),eq(5L));
    }
    @Test void colheitaComEstoqueExigeChaveIdempotente() {
        when(plantios.existsByCultivoId(5L)).thenReturn(true);
        var r=colheita(); r.setDestino(DestinoColheita.ESTOQUE); r.setItemEstoqueId(10L); r.setLocalEstoqueId(12L);
        assertThatThrownBy(() -> service.registrarColheita(5L,r)).hasMessageContaining("idempotencia");
        verify(estoque,never()).registrarEntradaAgriculturaColheita(any(),anyLong());
    }
    @ParameterizedTest @EnumSource(value=StatusCultivo.class,names={"COLHIDO","IMPLANTADO","PLANEJADO"})
    void statusNaoSubstituiRegistroOperacional(StatusCultivo status) {
        assertThatThrownBy(() -> service.alterarStatus(5L,new StatusCultivoRequest(status,0L))).hasMessageContaining("Transicao");
    }
    @Test void tarefaReusaVinculoExistente() {
        var r = new TarefaRequest(); r.setTitulo("Capina"); var ator = new UsuarioAtor(7L,"operador",false);
        service.criarTarefa(5L,r,ator); verify(tarefas).criarVinculada(r,ator,ModuloOrigem.AGRICULTURA,"CULTIVO:5");
    }
    @Test void leituraDeOutroCultivoRetorna404() {
        assertThatThrownBy(() -> service.detalharCultivo(99L)).isInstanceOfSatisfying(AgriculturaOperacaoException.class,
                e -> assertThat(e.getStatus().value()).isEqualTo(404));
    }
    @Test void paginaNegativaNormalizada() {
        when(cultivos.findByPropriedadeIdOrderByIdDesc(anyLong(),any())).thenReturn(org.springframework.data.domain.Page.empty());
        assertThat(service.listarCultivos(-1).conteudo()).isEmpty();
        verify(cultivos).findByPropriedadeIdOrderByIdDesc(eq(1L),argThat(p -> p.getPageNumber()==0 && p.getPageSize()==20));
    }
    @Test void fichaToleraFalhaDoResumoClimaticoLocal() {
        var local = mock(AgriculturaService.class); var clima = mock(ClimaConsultaService.class);
        var detalhe = new CultivoDetalhe(service.detalharCultivo(5L),List.of(),List.of(),List.of(),List.of(),ClimaResumo.naoSincronizado());
        when(local.detalheLocal(5L)).thenReturn(detalhe);
        when(clima.resumo()).thenThrow(new IllegalStateException("indisponivel"));
        assertThat(new AgriculturaFichaService(local,clima).detalhar(5L).clima().disponivel()).isFalse();
    }
    @Test void fichaMantemClimaSincronizado() {
        var local=mock(AgriculturaService.class); var clima=mock(ClimaConsultaService.class);
        var resumo= new ClimaResumo(true,true,BigDecimal.TEN,80,BigDecimal.ONE,null,null,null,null,null,DATA.atStartOfDay(),DATA.atStartOfDay(),"UTC","open-meteo");
        var detalhe = new CultivoDetalhe(service.detalharCultivo(5L),List.of(),List.of(),List.of(),List.of(),ClimaResumo.naoSincronizado());
        when(local.detalheLocal(5L)).thenReturn(detalhe);
        when(clima.resumo()).thenReturn(resumo);
        assertThat(new AgriculturaFichaService(local,clima).detalhar(5L).clima()).isSameAs(resumo);
    }
}
