package com.example.sitiopro.agricultura;

import com.example.sitiopro.agricultura.dto.*;
import com.example.sitiopro.agricultura.entity.*;
import com.example.sitiopro.agricultura.service.*;
import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.StatusCrs;
import com.example.sitiopro.propriedade.service.PerimetroService;
import com.example.sitiopro.propriedade.service.PropriedadeService;
import com.example.sitiopro.estoque.dto.*;
import com.example.sitiopro.estoque.entity.TipoMovimentoEstoque;
import com.example.sitiopro.estoque.service.*;
import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import com.example.sitiopro.tarefas.dto.TarefaRequest;
import com.example.sitiopro.tarefas.service.UsuarioAtor;
import com.example.sitiopro.usuario.entity.*;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name="openMeteoRestClient",classes=RestClient.class)
@MockBean(name="agrofitRestClient",classes=RestClient.class)
@MockBean(classes=SistemaSaudeService.class)
@SpringBootTest(properties={
        "spring.profiles.active=test", "spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false", "sitiopro.configuracao-operacional.inicial.timezone=UTC"
})
class AgriculturaSqlServerIntegrationTests {
    @Container static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();
    @DynamicPropertySource static void banco(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",SQLSERVER::getJdbcUrl); r.add("spring.datasource.username",SQLSERVER::getUsername);
        r.add("spring.datasource.password",SQLSERVER::getPassword); r.add("spring.flyway.user",SQLSERVER::getUsername);
        r.add("spring.flyway.password",SQLSERVER::getPassword);
    }
    @Autowired AgriculturaService service;
    @Autowired AgriculturaFichaService fichas;
    @Autowired PropriedadeService propriedades;
    @Autowired PerimetroService perimetros;
    @Autowired EstoqueMovimentoService estoque;
    @Autowired EstoqueCatalogoService catalogo;
    @Autowired AgrofitCulturaRepository agrofit;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory emf;
    @Autowired PlatformTransactionManager transactionManager;
    private final LocalDate inicio=LocalDate.now(ZoneOffset.UTC).minusDays(30);

    String nome(String base) { return base+" "+UUID.randomUUID().toString().substring(0,8); }
    CultivoResumo cultivo() {
        var t=new TalhaoRequest(); t.setNome(nome("Talhao SQL")); t.setAreaHa(new BigDecimal("2.0000"));
        Long talhao=propriedades.salvarTalhao(null,t).id();
        return cultivo(talhao);
    }
    CultivoResumo cultivo(Long talhao) {
        var s=new SafraRequest(); s.setNome(nome("Safra SQL")); s.setAnoInicio(inicio.getYear()); s.setAnoFim(inicio.plusYears(1).getYear());
        s.setDataInicio(inicio.minusDays(10)); s.setStatus(StatusSafra.EM_ANDAMENTO);
        Long safra=service.salvarSafra(null,s).id();
        var c=new CulturaRequest(); c.setNomeComum(nome("Milho SQL")); c.setCicloDiasEstimado(25);
        Long cultura=service.salvarCultura(null,c).id();
        var r=new CultivoRequest(); r.setSafraId(safra); r.setCulturaId(cultura); r.setTalhaoId(talhao);
        r.setAreaCultivadaHa(BigDecimal.ONE); r.setDataPlantio(inicio);
        return service.salvarCultivo(null,r);
    }
    VerticePerimetroRequest verticePerimetro(int ordem, String latitude, String longitude) {
        var v=new VerticePerimetroRequest(); v.setOrdem(ordem); v.setLatitude(new BigDecimal(latitude));
        v.setLongitude(new BigDecimal(longitude)); v.setMarco("P"+ordem); return v;
    }
    VerticeTalhaoRequest verticeTalhao(int ordem, String latitude, String longitude) {
        var v=new VerticeTalhaoRequest(); v.setOrdem(ordem); v.setLatitude(new BigDecimal(latitude));
        v.setLongitude(new BigDecimal(longitude)); v.setMarco("T"+ordem); return v;
    }
    Long talhaoGeorreferenciado() {
        var p=perimetros.formulario(); p.getVertices().clear(); p.setStatusCrs(StatusCrs.CONFIRMADO);
        p.setCrs("EPSG:4674"); p.setDatum("SIRGAS 2000");
        p.getVertices().addAll(List.of(
                verticePerimetro(1,"-8.000000000","-63.000000000"),
                verticePerimetro(2,"-8.010000000","-63.000000000"),
                verticePerimetro(3,"-8.010000000","-63.010000000"),
                verticePerimetro(4,"-8.000000000","-63.010000000")));
        perimetros.salvar(p);
        var t=new TalhaoRequest(); t.setNome(nome("Talhao mapa SQL")); t.setAreaHa(new BigDecimal("1.0000"));
        t.getVertices().addAll(List.of(
                verticeTalhao(1,"-8.001000000","-63.001000000"),
                verticeTalhao(2,"-8.002000000","-63.001000000"),
                verticeTalhao(3,"-8.002000000","-63.002000000"),
                verticeTalhao(4,"-8.001000000","-63.002000000")));
        return propriedades.salvarTalhao(null,t).id();
    }
    PlantioRequest plantio(long versao) {
        var r=new PlantioRequest(); r.setData(inicio); r.setQuantidade(new BigDecimal("10.1250")); r.setUnidade("KG");
        r.setOrigem(OrigemPlantio.EXTERNA); r.setDescricaoOrigem("Fornecedor manual SQL"); r.setVersao(versao); return r;
    }
    ColheitaRequest colheita(long versao) {
        var r=new ColheitaRequest(); r.setData(inicio.plusDays(25)); r.setQuantidade(new BigDecimal("100.1234"));
        r.setUnidade("KG"); r.setPerdas(new BigDecimal("2.5000")); r.setVersao(versao); return r;
    }
    AcompanhamentoRequest acompanhamento(long versao) {
        var r=new AcompanhamentoRequest(); r.setDataHora(inicio.plusDays(10).atTime(10,30));
        r.setTipo(TipoAcompanhamentoCultivo.DESENVOLVIMENTO); r.setDescricao("Inspecao SQL"); r.setVersao(versao); return r;
    }

    @Test void v19AplicadaPreservaV18ComHibernateValidateEConstraintsConfiaveis() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version='17'",Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version='18'",Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version='19'",Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.tables WHERE name LIKE 'agricultura_%'",Integer.class)).isEqualTo(12);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.foreign_keys WHERE name LIKE 'fk_agricultura_%' AND (is_disabled=1 OR is_not_trusted=1)",Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.check_constraints WHERE name LIKE 'ck_agricultura_%' AND (is_disabled=1 OR is_not_trusted=1)",Integer.class)).isZero();
    }
    @Test @Transactional @WithMockUser(username="agricultor",roles="OPERADOR")
    void cicloCompletoPersisteAuditoriaPrevisaoEColheita() {
        Usuario usuario=new Usuario(); usuario.setNome("Agricultor"); usuario.setLogin("agricultor");
        usuario.setPerfil(PerfilUsuario.OPERADOR); usuario.setSenhaHash(encoder.encode(UUID.randomUUID().toString()));
        usuarios.saveAndFlush(usuario);
        var principal=new com.example.sitiopro.usuario.security.UsuarioPrincipal(usuario);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal,null,principal.getAuthorities()));
        var c=cultivo();
        assertThat(c.previsaoColheita()).isEqualTo(inicio.plusDays(25));
        assertThat(c.talhaoCodigo()).startsWith("TL-");
        service.registrarPlantio(c.id(),plantio(c.versao()));
        var plantado=service.detalharCultivo(c.id());
        assertThat(plantado.status()).isEqualTo(StatusCultivo.IMPLANTADO);
        assertThat(plantado.versao()).isGreaterThan(c.versao());
        service.registrarAcompanhamento(c.id(),acompanhamento(plantado.versao()));
        var acompanhado=service.detalharCultivo(c.id());
        service.registrarColheita(c.id(),colheita(acompanhado.versao()));
        var ficha=service.detalheLocal(c.id());
        assertThat(ficha.resumo().status()).isEqualTo(StatusCultivo.COLHIDO);
        assertThat(ficha.colheitas().getFirst().quantidade()).isEqualByComparingTo("100.1234");
        assertThat(ficha.colheitas().getFirst().perdas()).isEqualByComparingTo("2.5");
        assertThat(ficha.acompanhamentos().getFirst().responsavel()).isEqualTo("agricultor");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM estoque_movimentos WHERE origem_modulo='agricultura' AND origem_referencia_id=?",Integer.class,c.id())).isZero();
    }
    @Test @Transactional void retryNaoDuplicaPlantioNemColheita() {
        var c=cultivo(); var plantio=plantio(c.versao()); service.registrarPlantio(c.id(),plantio);
        assertThatThrownBy(() -> service.registrarPlantio(c.id(),plantio)).hasMessageContaining("Recarregue");
        var h=colheita(service.detalharCultivo(c.id()).versao()); service.registrarColheita(c.id(),h);
        assertThatThrownBy(() -> service.registrarColheita(c.id(),h)).hasMessageContaining("Recarregue");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM agricultura_plantios WHERE cultivo_id=?",Integer.class,c.id())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM agricultura_colheitas WHERE cultivo_id=?",Integer.class,c.id())).isEqualTo(1);
    }
    @Test @Transactional void colheitasParciaisEFinalMantemUnidadesSemSomarUnidadesDiferentes() {
        var c=cultivo(); service.registrarPlantio(c.id(),plantio(c.versao()));
        var h=colheita(service.detalharCultivo(c.id()).versao()); h.setFinalizaCultivo(false); service.registrarColheita(c.id(),h);
        assertThat(service.detalharCultivo(c.id()).status()).isEqualTo(StatusCultivo.PRONTO_COLHEITA);
        h=colheita(service.detalharCultivo(c.id()).versao()); service.registrarColheita(c.id(),h);
        assertThat(service.detalheLocal(c.id()).colheitas()).hasSize(2);
    }
    @Test @Transactional void safraEditaComVersaoEFechaApenasAposCultivos() {
        var c=cultivo(); var s=service.formularioSafra(c.safraId()); s.setStatus(StatusSafra.ENCERRADA);
        assertThatThrownBy(() -> service.salvarSafra(c.safraId(),s)).hasMessageContaining("Finalize");
        service.alterarStatus(c.id(),new StatusCultivoRequest(StatusCultivo.CANCELADO,c.versao()));
        var fechada=service.salvarSafra(c.safraId(),s);
        assertThat(fechada.status()).isEqualTo(StatusSafra.ENCERRADA); assertThat(fechada.versao()).isGreaterThan(s.getVersao());
    }
    @Test @Transactional void culturaPodeAssociarAgrofitSemInternet() {
        var ref=agrofit.saveAndFlush(new AgrofitCultura(nome("Milho oficial"),nome("MILHO"),LocalDateTime.now()));
        var c=new CulturaRequest(); c.setNomeComum(nome("Cultura interna")); c.setAgrofitCulturaId(ref.getId());
        var salva=service.salvarCultura(null,c);
        assertThat(salva.agrofitCulturaId()).isEqualTo(ref.getId()); assertThat(salva.agrofitNome()).isEqualTo(ref.getNome());
        var r=service.formularioCultura(salva.id()); r.setAtivo(false); r.setCicloDiasEstimado(30);
        assertThat(service.salvarCultura(salva.id(),r).ativo()).isFalse();
    }
    @Test @Transactional void bancoImpedeCruzarTalhaoESafraDePropriedadesDiferentes() {
        var c=cultivo();
        jdbc.update("INSERT INTO propriedades (nome) VALUES (?)",nome("Outra"));
        Long outra=jdbc.queryForObject("SELECT MAX(id) FROM propriedades",Long.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE agricultura_cultivos SET propriedade_id=? WHERE id=?",outra,c.id()))
                .hasStackTraceContaining("fk_agricultura_cultivos");
    }
    @ParameterizedTest @ValueSource(strings={"0","-1"})
    @Transactional void bancoRejeitaAreaNaoPositiva(String valor) {
        var c=cultivo();
        assertThatThrownBy(() -> jdbc.update("UPDATE agricultura_cultivos SET area_cultivada_ha=? WHERE id=?",new BigDecimal(valor),c.id()))
                .hasStackTraceContaining("ck_agricultura_cultivos_area");
    }
    @Test @Transactional void bancoExigeTalhaoOficial() {
        var c=cultivo();
        assertThatThrownBy(() -> jdbc.update("UPDATE agricultura_cultivos SET talhao_id=999999 WHERE id=?",c.id()))
                .hasStackTraceContaining("fk_agricultura_cultivos_talhao");
    }
    @Test @Transactional void bancoNaoAceitaColhidoSemData() {
        var c=cultivo();
        assertThatThrownBy(() -> jdbc.update("UPDATE agricultura_cultivos SET status='COLHIDO' WHERE id=?",c.id()))
                .hasStackTraceContaining("ck_agricultura_cultivos_colheita");
    }
    @Test @Transactional void painelConsultaTarefasRelacionadasEAlertasDeColheita() {
        var c=cultivo();
        Usuario u=new Usuario(); u.setNome("Operador SQL"); u.setLogin(nome("op").replace(" ",""));
        u.setPerfil(PerfilUsuario.OPERADOR); u.setSenhaHash(encoder.encode(UUID.randomUUID().toString()));
        usuarios.saveAndFlush(u);
        var tarefa=new TarefaRequest(); tarefa.setTitulo("Capina SQL"); tarefa.setDataVencimento(LocalDateTime.now().plusDays(1));
        var t=service.criarTarefa(c.id(),tarefa,new UsuarioAtor(u.getId(),u.getLogin(),false));
        assertThat(service.detalheLocal(c.id()).tarefas()).extracting("id").contains(t.id());
        assertThat(service.painel().proximosTrabalhos()).extracting("id").contains(t.id());
        assertThat(service.painel().alertas()).extracting(AvisoAgricola::cultivoId).contains(c.id());
        service.alterarStatus(c.id(),new StatusCultivoRequest(StatusCultivo.CANCELADO,c.versao()));
        assertThat(service.painel().alertas()).extracting(AvisoAgricola::cultivoId).doesNotContain(c.id());
    }
    @Test @Transactional void mapaOperacionalConsultaSqlLocalComTalhaoGeorreferenciado() {
        var c=cultivo(talhaoGeorreferenciado());
        service.registrarPlantio(c.id(),plantio(c.versao()));
        var o=new OcorrenciaRequest(); o.setDataHora(inicio.plusDays(2).atTime(8,30));
        o.setTipo(TipoOcorrenciaCultivo.PRAGA); o.setSeveridade(SeveridadeOcorrencia.CRITICA);
        o.setTitulo("Inspecao mapa SQL"); o.setDescricao("Ocorrencia aberta para o mapa operacional.");
        o.setChaveIdempotencia("mapa-sql-"+UUID.randomUUID()); o.setVersao(service.detalharCultivo(c.id()).versao());
        service.registrarOcorrencia(c.id(),o);

        var mapa=service.mapaOperacional();

        assertThat(mapa.crs()).isEqualTo("EPSG:4674");
        assertThat(mapa.talhoes()).anySatisfy(t -> {
            assertThat(t.id()).isEqualTo(c.talhaoId());
            assertThat(t.vertices()).hasSize(4);
            assertThat(t.cultivoAtivo()).isNotNull();
            assertThat(t.cultivoAtivo().id()).isEqualTo(c.id());
            assertThat(t.cultivoAtivo().severidadeMaisAlta()).isEqualTo(SeveridadeOcorrencia.CRITICA);
            assertThat(t.cultivoAtivo().ocorrenciasAbertas()).isEqualTo(1);
        });
    }
    @Test @Transactional void fichaSemClimaContinuaDisponivel() {
        var c=cultivo(); var ficha=fichas.detalhar(c.id());
        assertThat(ficha.resumo().id()).isEqualTo(c.id()); assertThat(ficha.clima().disponivel()).isFalse();
    }
    record Insumo(Long item,Long local) {}
    Insumo insumo() {
        var categoria=new CategoriaEstoqueRequest(); categoria.setNome(nome("Insumos"));
        var item=new ItemEstoqueRequest(); item.setNome(nome("Sementes")); item.setCategoriaId(catalogo.criarCategoria(categoria).getId());
        item.setUnidadeMedidaId(jdbc.queryForObject("SELECT id FROM estoque_unidades_medida WHERE sigla='KG'",Long.class));
        Long itemId=catalogo.criarItem(item).getId();
        Long local=jdbc.queryForObject("SELECT id FROM estoque_locais WHERE nome='Agricultura'",Long.class);
        var entrada=new MovimentoEstoqueRequest(); entrada.setItemId(itemId); entrada.setTipo(TipoMovimentoEstoque.ENTRADA);
        entrada.setQuantidade(new BigDecimal("20")); entrada.setLocalDestinoId(local);
        estoque.registrarMovimento(entrada,false); return new Insumo(itemId,local);
    }
    @Test @Transactional void consumoUsaSaldoOficialERetryNaoDescontaNovamente() {
        var c=cultivo(); var i=insumo(); var r=plantio(c.versao());
        r.setOrigem(OrigemPlantio.ESTOQUE); r.setItemEstoqueId(i.item()); r.setLocalEstoqueId(i.local());
        var p=service.registrarPlantio(c.id(),r);
        assertThat(p.movimentoEstoqueId()).isPositive(); assertThat(estoque.saldoItemTotal(i.item())).isEqualByComparingTo("9.8750");
        assertThatThrownBy(() -> service.registrarPlantio(c.id(),r)).hasMessageContaining("Recarregue");
        assertThat(estoque.saldoItemTotal(i.item())).isEqualByComparingTo("9.8750");
        assertThat(jdbc.queryForObject("SELECT origem_modulo FROM estoque_movimentos WHERE id=?",String.class,p.movimentoEstoqueId())).isEqualTo("agricultura");
    }
    @Test @Transactional void operacoesDeCampoSaoAuditadasEIdempotentes() {
        var c=cultivo(); service.registrarPlantio(c.id(),plantio(c.versao()));
        var a=new AdubacaoRequest(); a.setData(inicio.plusDays(5)); a.setProduto("Composto SQL");
        a.setQuantidade(new BigDecimal("2.2500")); a.setUnidade("KG"); a.setDescricaoOrigem("Origem externa SQL");
        a.setChaveIdempotencia("adubacao-sql-001"); a.setVersao(service.detalharCultivo(c.id()).versao());
        var primeira=service.registrarAdubacao(c.id(),a); var repetida=service.registrarAdubacao(c.id(),a);
        assertThat(repetida.id()).isEqualTo(primeira.id());
        var i=new IrrigacaoRequest(); i.setDataHora(inicio.plusDays(6).atTime(8,0)); i.setDuracaoMinutos(40);
        i.setVolumeLitros(new BigDecimal("900.5000")); i.setChaveIdempotencia("irrigacao-sql-001");
        i.setVersao(service.detalharCultivo(c.id()).versao()); service.registrarIrrigacao(c.id(),i);
        var t=new TratamentoRequest(); t.setData(inicio.plusDays(7)); t.setFinalidade("Ocorrencia observada");
        t.setProdutoAplicado("Produto externo"); t.setQuantidade(BigDecimal.ONE); t.setUnidade("L");
        t.setDescricaoOrigem("Fornecedor SQL"); t.setChaveIdempotencia("tratamento-sql-001");
        t.setVersao(service.detalharCultivo(c.id()).versao()); service.registrarTratamento(c.id(),t);
        var o=new OcorrenciaRequest(); o.setDataHora(inicio.plusDays(8).atTime(9,0)); o.setTipo(TipoOcorrenciaCultivo.DANO_CLIMATICO);
        o.setSeveridade(SeveridadeOcorrencia.MEDIA); o.setTitulo("Dano climatico SQL"); o.setDescricao("Perda operacional SQL");
        o.setQuantidadePerdida(new BigDecimal("1.5000")); o.setUnidadePerda("KG");
        o.setChaveIdempotencia("ocorrencia-sql-001"); o.setVersao(service.detalharCultivo(c.id()).versao());
        service.registrarOcorrencia(c.id(),o);
        var ficha=service.detalheLocal(c.id()); assertThat(ficha.adubacoes()).hasSize(1);
        assertThat(ficha.irrigacoes()).singleElement().extracting(IrrigacaoResumo::volumeLitros).isEqualTo(new BigDecimal("900.5000"));
        assertThat(ficha.tratamentos()).hasSize(1); assertThat(ficha.ocorrencias()).hasSize(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM agricultura_adubacoes WHERE cultivo_id=?",Integer.class,c.id())).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT criado_por FROM agricultura_adubacoes WHERE id=?",String.class,primeira.id())).isNotBlank();
    }
    @Test @Transactional void ocorrenciaFitossanitariaMantemHistoricoAgrofitAlertaETarefaIdempotente() {
        Usuario usuario=new Usuario(); usuario.setNome("Fitossanidade SQL"); usuario.setLogin(nome("fito").replace(" ",""));
        usuario.setPerfil(PerfilUsuario.OPERADOR); usuario.setSenhaHash(encoder.encode(UUID.randomUUID().toString()));
        usuarios.saveAndFlush(usuario);
        var principal=new com.example.sitiopro.usuario.security.UsuarioPrincipal(usuario);
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal,null,principal.getAuthorities()));
        var c=cultivo();
        service.registrarPlantio(c.id(),plantio(c.versao()));
        c=service.detalharCultivo(c.id());
        var referencia=agrofit.saveAndFlush(new AgrofitCultura(
                nome("Milho Agrofit SQL"),nome("Zea mays SQL"),LocalDateTime.now()));
        var registro=new OcorrenciaRequest(); registro.setDataHora(inicio.plusDays(8).atTime(9,0));
        registro.setTipo(TipoOcorrenciaCultivo.PRAGA); registro.setSeveridade(SeveridadeOcorrencia.ALTA);
        registro.setTitulo("Lagarta observada"); registro.setDescricao("Focos localizados nas folhas.");
        registro.setAreaAfetadaHa(new BigDecimal("0.2500")); registro.setAgrofitCulturaIds(List.of(referencia.getId()));
        registro.setChaveIdempotencia("fito-registro-sql-001"); registro.setVersao(c.versao());

        var criada=service.registrarOcorrencia(c.id(),registro);
        assertThat(criada.status()).isEqualTo(StatusOcorrenciaCultivo.ABERTA);
        assertThat(criada.referenciasAgrofit()).extracting(ReferenciaAgrofitResumo::id).containsExactly(referencia.getId());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM alertas WHERE chave_deduplicacao=? AND status='ATIVO'",
                Integer.class,"AGRICULTURA:OCORRENCIA:"+criada.id()+":FITOSSANITARIA")).isEqualTo(1);

        var atualizacao=new OcorrenciaAtualizacaoRequest(); atualizacao.setTipo(TipoOcorrenciaCultivo.PRAGA);
        atualizacao.setSeveridade(SeveridadeOcorrencia.CRITICA); atualizacao.setTitulo("Lagarta em expansao");
        atualizacao.setDescricao("Novos focos identificados durante a inspecao.");
        atualizacao.setAreaAfetadaHa(new BigDecimal("0.5000")); atualizacao.setAgrofitCulturaIds(List.of(referencia.getId()));
        atualizacao.setAcompanhamento("Area demarcada para novo acompanhamento.");
        atualizacao.setChaveIdempotencia("fito-acomp-sql-001"); atualizacao.setVersao(criada.versao());
        var atualizada=service.atualizarOcorrencia(criada.id(),atualizacao);
        assertThat(service.atualizarOcorrencia(criada.id(),atualizacao).id()).isEqualTo(criada.id());
        assertThat(atualizada.status()).isEqualTo(StatusOcorrenciaCultivo.EM_ACOMPANHAMENTO);

        UsuarioAtor ator=new UsuarioAtor(usuario.getId(),usuario.getLogin(),false);
        var tarefa=service.criarTarefaInspecao(criada.id(),ator);
        assertThat(service.criarTarefaInspecao(criada.id(),ator).id()).isEqualTo(tarefa.id());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM tarefas WHERE chave_automacao=?",Integer.class,
                "AGRICULTURA:OCORRENCIA:"+criada.id()+":INSPECAO")).isEqualTo(1);

        var encerramento=new EncerramentoOcorrenciaRequest(); encerramento.setDataHora(inicio.plusDays(12).atTime(15,0));
        encerramento.setResolucao("Focos controlados e area mantida em observacao.");
        encerramento.setChaveIdempotencia("fito-encerra-sql-001"); encerramento.setVersao(atualizada.versao());
        var encerrada=service.encerrarOcorrencia(criada.id(),encerramento);
        assertThat(service.encerrarOcorrencia(criada.id(),encerramento).id()).isEqualTo(criada.id());
        assertThat(encerrada.status()).isEqualTo(StatusOcorrenciaCultivo.ENCERRADA);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM agricultura_ocorrencia_historicos WHERE ocorrencia_id=?",
                Integer.class,criada.id())).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT status FROM alertas WHERE chave_deduplicacao=?",String.class,
                "AGRICULTURA:OCORRENCIA:"+criada.id()+":FITOSSANITARIA")).isEqualTo("RESOLVIDO");
        assertThat(jdbc.queryForObject("SELECT criado_por FROM agricultura_ocorrencias WHERE id=?",String.class,criada.id()))
                .isEqualTo(usuario.getLogin());
    }
    @Test @Transactional void insumosDeCampoConsomemEstoqueUmaVez() {
        var c=cultivo(); service.registrarPlantio(c.id(),plantio(c.versao())); var insumo=insumo();
        var t=new TratamentoRequest(); t.setData(inicio.plusDays(5)); t.setFinalidade("Controle SQL");
        t.setProdutoAplicado("Ignorado pelo item oficial"); t.setQuantidade(new BigDecimal("3.0000")); t.setUnidade("KG");
        t.setOrigem(OrigemInsumo.ESTOQUE); t.setItemEstoqueId(insumo.item()); t.setLocalEstoqueId(insumo.local());
        t.setChaveIdempotencia("tratamento-estoque-sql"); t.setVersao(service.detalharCultivo(c.id()).versao());
        var primeiro=service.registrarTratamento(c.id(),t); var retry=service.registrarTratamento(c.id(),t);
        assertThat(retry.id()).isEqualTo(primeiro.id()); assertThat(estoque.saldoItemTotal(insumo.item())).isEqualByComparingTo("17");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM estoque_movimentos WHERE origem_modulo='agricultura' AND origem_referencia_id=? AND tipo='CONSUMO'",Integer.class,c.id())).isEqualTo(1);
    }
    @Test @Transactional void colheitaPodeEntrarNoEstoqueSemDuplicarNoRetry() {
        var c=cultivo(); service.registrarPlantio(c.id(),plantio(c.versao())); var destino=insumo();
        var h=colheita(service.detalharCultivo(c.id()).versao()); h.setDestino(DestinoColheita.ESTOQUE);
        h.setItemEstoqueId(destino.item()); h.setLocalEstoqueId(destino.local()); h.setChaveIdempotencia("colheita-estoque-sql");
        var primeira=service.registrarColheita(c.id(),h); var retry=service.registrarColheita(c.id(),h);
        assertThat(retry.id()).isEqualTo(primeira.id()); assertThat(primeira.movimentoEstoqueId()).isPositive();
        assertThat(estoque.saldoItemTotal(destino.item())).isEqualByComparingTo("120.1234");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM estoque_movimentos WHERE origem_modulo='agricultura' AND origem_referencia_id=? AND tipo='ENTRADA'",Integer.class,c.id())).isEqualTo(1);
    }
    @Test void rollbackExternoReverteColheitaEEntradaNaMesmaTransacao() {
        var c=cultivo(); service.registrarPlantio(c.id(),plantio(c.versao())); var destino=insumo();
        long versao=service.detalharCultivo(c.id()).versao();
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var h=colheita(versao); h.setDestino(DestinoColheita.ESTOQUE); h.setItemEstoqueId(destino.item());
            h.setLocalEstoqueId(destino.local()); h.setChaveIdempotencia("colheita-rollback-sql");
            service.registrarColheita(c.id(),h); throw new IllegalStateException("falha posterior controlada");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(estoque.saldoItemTotal(destino.item())).isEqualByComparingTo("20");
        assertThat(service.detalheLocal(c.id()).colheitas()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM estoque_movimentos WHERE origem_modulo='agricultura' AND origem_referencia_id=?",Integer.class,c.id())).isZero();
    }
    @Test void saldoInsuficienteRevertePlantioEVersao() {
        var c=cultivo(); var i=insumo(); var r=plantio(c.versao());
        r.setOrigem(OrigemPlantio.ESTOQUE); r.setItemEstoqueId(i.item()); r.setLocalEstoqueId(i.local()); r.setQuantidade(new BigDecimal("21"));
        assertThatThrownBy(() -> service.registrarPlantio(c.id(),r)).hasMessageContaining("Saldo insuficiente");
        assertThat(service.detalheLocal(c.id()).plantios()).isEmpty();
        assertThat(service.detalharCultivo(c.id()).versao()).isEqualTo(c.versao());
        assertThat(estoque.saldoItemTotal(i.item())).isEqualByComparingTo("20");
    }
    @Test void falhaAposMovimentacaoReverteConsumoEPlantioNaMesmaTransacao() {
        var c=cultivo(); var i=insumo();
        var edicao=service.formularioCultivo(c.id()); edicao.setPrevisaoColheita(inicio.plusDays(1));
        c=service.salvarCultivo(c.id(),edicao);
        var r=plantio(c.versao()); r.setData(inicio.plusDays(2));
        r.setOrigem(OrigemPlantio.ESTOQUE); r.setItemEstoqueId(i.item()); r.setLocalEstoqueId(i.local());
        Long id=c.id(); long versao=c.versao();
        assertThatThrownBy(() -> service.registrarPlantio(id,r)).hasMessageContaining("Revise a previsao");
        assertThat(estoque.saldoItemTotal(i.item())).isEqualByComparingTo("20");
        assertThat(service.detalheLocal(id).plantios()).isEmpty();
        assertThat(service.detalharCultivo(id).versao()).isEqualTo(versao);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM estoque_movimentos WHERE origem_modulo='agricultura' AND origem_referencia_id=?",Integer.class,id)).isZero();
    }
    @Test @Transactional void periodoDaSafraPreservaAcompanhamentosAntesDoPlantio() {
        var c=cultivo(); var a=acompanhamento(c.versao()); a.setDataHora(inicio.minusDays(5).atTime(10,0));
        service.registrarAcompanhamento(c.id(),a);
        var s=service.formularioSafra(c.safraId()); s.setDataInicio(inicio);
        assertThatThrownBy(() -> service.salvarSafra(c.safraId(),s)).hasMessageContaining("excluir datas");
    }
    @Test @Transactional void colheitaNaoPodeAntecederReplantio() {
        var c=cultivo(); service.registrarPlantio(c.id(),plantio(c.versao()));
        var p=plantio(service.detalharCultivo(c.id()).versao()); p.setData(inicio.plusDays(10));
        service.registrarPlantio(c.id(),p);
        var h=colheita(service.detalharCultivo(c.id()).versao()); h.setData(inicio.plusDays(5));
        assertThatThrownBy(() -> service.registrarColheita(c.id(),h)).hasMessageContaining("ultimo plantio");
    }
    @Test void duasReservasConcorrentesNaoUltrapassamTalhao() throws Exception {
        var c=cultivo();
        try (var executor=Executors.newFixedThreadPool(2)) {
            var pronto=new CountDownLatch(2); var iniciar=new CountDownLatch(1);
            Callable<Boolean> criar=() -> {
                var r=service.formularioCultivo(c.id()); r.setAreaCultivadaHa(new BigDecimal("0.75"));
                pronto.countDown(); if(!iniciar.await(10,TimeUnit.SECONDS)) throw new IllegalStateException("Inicio nao liberado");
                try { service.salvarCultivo(null,r); return true; }
                catch (AgriculturaOperacaoException ex) { assertThat(ex.getMessage()).contains("excede"); return false; }
            };
            var f1=executor.submit(criar); var f2=executor.submit(criar);
            assertThat(pronto.await(10,TimeUnit.SECONDS)).isTrue(); iniciar.countDown();
            assertThat(List.of(f1.get(30,TimeUnit.SECONDS),f2.get(30,TimeUnit.SECONDS))).containsExactlyInAnyOrder(true,false);
        }
        assertThat(jdbc.queryForObject("SELECT SUM(area_cultivada_ha) FROM agricultura_cultivos WHERE talhao_id=?",BigDecimal.class,c.talhaoId())).isEqualByComparingTo("1.75");
    }
    @Test void optimisticLockingRealRejeitaEscritaObsoleta() {
        var c=cultivo();
        try(var e1=emf.createEntityManager();var e2=emf.createEntityManager()) {
            e1.getTransaction().begin(); e2.getTransaction().begin();
            var a=e1.find(Cultivo.class,c.id()); var b=e2.find(Cultivo.class,c.id());
            a.setObservacao("Vencedor"); e1.getTransaction().commit(); b.setObservacao("Obsoleto");
            assertThatThrownBy(() -> e2.getTransaction().commit()).isInstanceOf(jakarta.persistence.RollbackException.class);
        }
        assertThat(service.detalharCultivo(c.id()).observacao()).isEqualTo("Vencedor");
    }
    @Test void upgradeV16PreservaTalhaoEConfiguracoes() {
        jdbc.execute("CREATE DATABASE agricultura_upgrade");
        SQLServerDataSource ds=new SQLServerDataSource();
        ds.setServerName(SQLSERVER.getHost()); ds.setPortNumber(SQLSERVER.getMappedPort(1433));
        ds.setDatabaseName("agricultura_upgrade"); ds.setUser(SQLSERVER.getUsername()); ds.setPassword(SQLSERVER.getPassword());
        ds.setEncrypt("false"); ds.setTrustServerCertificate(true);
        Flyway.configure().dataSource(ds).target("16").load().migrate();
        JdbcTemplate upgrade=new JdbcTemplate(ds);
        upgrade.update("INSERT INTO propriedades (nome,principal) VALUES ('Legada',1)");
        Long id=upgrade.queryForObject("SELECT id FROM propriedades",Long.class);
        upgrade.update("INSERT INTO propriedade_talhoes (propriedade_id,nome,area_ha,status) VALUES (?,'Campo antigo',2.1234,'ATIVO')",id);
        String codigo=upgrade.queryForObject("SELECT codigo FROM propriedade_talhoes",String.class);
        Flyway.configure().dataSource(ds).load().migrate();
        assertThat(upgrade.queryForObject("SELECT codigo FROM propriedade_talhoes",String.class)).isEqualTo(codigo);
        assertThat(upgrade.queryForObject("SELECT area_ha FROM propriedade_talhoes",BigDecimal.class)).isEqualByComparingTo("2.1234");
        assertThat(upgrade.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1",Integer.class)).isEqualTo(22);
        assertThat(upgrade.queryForObject("SELECT COUNT(*) FROM agricultura_cultivos",Integer.class)).isZero();
    }
    @Test void upgradeV18ConverteOcorrenciaLegadaSemPerderDados() {
        jdbc.execute("CREATE DATABASE agricultura_v19_upgrade");
        SQLServerDataSource ds=new SQLServerDataSource();
        ds.setServerName(SQLSERVER.getHost()); ds.setPortNumber(SQLSERVER.getMappedPort(1433));
        ds.setDatabaseName("agricultura_v19_upgrade"); ds.setUser(SQLSERVER.getUsername()); ds.setPassword(SQLSERVER.getPassword());
        ds.setEncrypt("false"); ds.setTrustServerCertificate(true);
        Flyway.configure().dataSource(ds).target("18").load().migrate();
        JdbcTemplate upgrade=new JdbcTemplate(ds);
        upgrade.update("INSERT INTO propriedades (nome,principal) VALUES ('Legada V18',1)");
        Long propriedade=upgrade.queryForObject("SELECT id FROM propriedades",Long.class);
        upgrade.update("INSERT INTO propriedade_talhoes (propriedade_id,nome,area_ha,status) VALUES (?,'Talhao V18',2.0000,'ATIVO')",propriedade);
        Long talhao=upgrade.queryForObject("SELECT id FROM propriedade_talhoes",Long.class);
        upgrade.update("INSERT INTO agricultura_safras (propriedade_id,nome,ano_inicio,ano_fim,data_inicio,status) VALUES (?,'Safra V18',2026,2027,'2026-01-01','EM_ANDAMENTO')",propriedade);
        Long safra=upgrade.queryForObject("SELECT id FROM agricultura_safras",Long.class);
        upgrade.update("INSERT INTO agricultura_culturas (nome_comum,ciclo_dias_estimado) VALUES ('Milho V18',120)");
        Long cultura=upgrade.queryForObject("SELECT id FROM agricultura_culturas",Long.class);
        upgrade.update("INSERT INTO agricultura_cultivos (propriedade_id,safra_id,talhao_id,cultura_id,area_cultivada_ha,data_plantio,status) VALUES (?,?,?,?,1.0000,'2026-02-01','IMPLANTADO')",
                propriedade,safra,talhao,cultura);
        Long cultivo=upgrade.queryForObject("SELECT id FROM agricultura_cultivos",Long.class);
        upgrade.update("INSERT INTO agricultura_ocorrencias (cultivo_id,data_hora,tipo,severidade,descricao,chave_idempotencia) VALUES (?,'2026-02-15T09:00:00','CLIMATICA','ALTA','Dano por vento','ocorrencia-v18-001')",cultivo);
        Long ocorrencia=upgrade.queryForObject("SELECT id FROM agricultura_ocorrencias",Long.class);

        Flyway.configure().dataSource(ds).load().migrate();

        assertThat(upgrade.queryForObject("SELECT tipo FROM agricultura_ocorrencias WHERE id=?",String.class,ocorrencia))
                .isEqualTo("DANO_CLIMATICO");
        assertThat(upgrade.queryForObject("SELECT titulo FROM agricultura_ocorrencias WHERE id=?",String.class,ocorrencia))
                .isEqualTo("Dano por vento");
        assertThat(upgrade.queryForObject("SELECT status FROM agricultura_ocorrencias WHERE id=?",String.class,ocorrencia))
                .isEqualTo("ABERTA");
        assertThat(upgrade.queryForObject("SELECT versao FROM agricultura_ocorrencias WHERE id=?",Long.class,ocorrencia)).isZero();
        assertThat(upgrade.queryForObject("SELECT COUNT(*) FROM agricultura_ocorrencia_historicos WHERE ocorrencia_id=?",Integer.class,ocorrencia))
                .isEqualTo(1);
        assertThat(upgrade.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1",Integer.class)).isEqualTo(22);
    }
}
