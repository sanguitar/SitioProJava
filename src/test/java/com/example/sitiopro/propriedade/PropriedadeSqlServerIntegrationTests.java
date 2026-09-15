package com.example.sitiopro.propriedade;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.propriedade.service.*;
import com.example.sitiopro.criacao.aves.service.InstalacaoCriacaoService;
import com.example.sitiopro.criacao.core.dto.InstalacaoCriacaoRequest;
import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import com.example.sitiopro.criacao.core.repository.InstalacaoCriacaoRepository;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name = "openMeteoRestClient", classes = RestClient.class)
@MockBean(name = "agrofitRestClient", classes = RestClient.class)
@MockBean(classes = SistemaSaudeService.class)
@SpringBootTest(properties = {
        "spring.profiles.active=test", "spring.jpa.hibernate.ddl-auto=validate", "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false", "sitiopro.configuracao-operacional.inicial.nome-propriedade=Sítio físico SQL"
})
class PropriedadeSqlServerIntegrationTests {
    @Container static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();
    @DynamicPropertySource static void banco(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", SQLSERVER::getJdbcUrl);
        r.add("spring.datasource.username", SQLSERVER::getUsername);
        r.add("spring.datasource.password", SQLSERVER::getPassword);
        r.add("spring.flyway.user", SQLSERVER::getUsername);
        r.add("spring.flyway.password", SQLSERVER::getPassword);
    }
    @Autowired PropriedadeService service;
    @Autowired PerimetroService perimetros;
    @jakarta.persistence.PersistenceContext jakarta.persistence.EntityManager entityManager;
    @Autowired ConfiguracaoOperacionalService config;
    @Autowired InstalacaoCriacaoService criacoes;
    @Autowired InstalacaoCriacaoRepository instalacoes;
    @Autowired PropriedadeRepository propriedades;
    @Autowired JdbcTemplate jdbc;
    @Autowired EntityManagerFactory emf;

    @Test void v21ConfirmaSirgas2000ComGeographyEValoresDocumentaisSeparados() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version='20'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version='21'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.columns c JOIN sys.types t ON c.user_type_id=t.user_type_id WHERE c.object_id = OBJECT_ID('propriedade_perimetros') AND c.name = 'poligono_geography' AND t.name = 'geography'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.columns c JOIN sys.types t ON c.user_type_id=t.user_type_id WHERE c.object_id = OBJECT_ID('propriedade_talhoes') AND c.name = 'geometria_geography' AND t.name = 'geography'", Integer.class)).isEqualTo(1);
        var p = perimetros.obter();
        assertThat(p.statusCrs()).isEqualTo(StatusCrs.CONFIRMADO);
        assertThat(p.crs()).isEqualTo("EPSG:4674");
        assertThat(p.datum()).isEqualTo("SIRGAS 2000");
        assertThat(p.conferencia().sistemaGeodesico()).isEqualTo("SIRGAS 2000");
        assertThat(p.conferencia().crsEpsg()).isEqualTo(4674);
        assertThat(p.conferencia().areaDocumentalHa()).isEqualByComparingTo("1.8955");
        assertThat(p.conferencia().perimetroDocumentalM()).isEqualByComparingTo("919.71");
        assertThat(p.conferencia().areaCalculadaM2()).isEqualByComparingTo("18954.7120");
        assertThat(p.conferencia().perimetroCalculadoM()).isEqualByComparingTo("919.7012");
        assertThat(p.conferencia().geometriaValida()).isTrue();
        assertThat(p.vertices()).extracting(PerimetroResumo.Vertice::ordem).containsExactly(1,2,3,4);
        assertThat(p.vertices()).extracting(PerimetroResumo.Vertice::marco)
                .containsExactly("DZCZ-M-0205","DZCZ-M-0168","DZCZ-M-0167","DZCZ-M-0170");
        assertThat(p.vertices().getFirst().longitude()).isEqualByComparingTo("-63.871070000");
        assertThat(p.vertices().getFirst().latitude()).isEqualByComparingTo("-8.346821111");
        assertThat(p.vertices().getFirst().altitudeGeodesicaM()).isEqualByComparingTo("90.30");
        assertThat(p.getGeoJson().geometry().type()).isEqualTo("Polygon");
        assertThat(p.getGeoJson().geometry().coordinates().toString()).contains("-63.871070000", "-8.346821111", "90.30");
    }

    @Test @Transactional void v22TalhaoGeorreferenciadoUsaSirgas2000EPermaneceDentroDoPerimetro() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version='22'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.columns c JOIN sys.types t ON c.user_type_id=t.user_type_id WHERE c.object_id = OBJECT_ID('propriedade_talhoes') AND c.name = 'geometria_geography' AND t.name = 'geography'", Integer.class)).isEqualTo(1);

        var r = new TalhaoRequest();
        r.setNome("Talhão georreferenciado");
        r.setAreaHa(new BigDecimal("0.1000"));
        r.getVertices().addAll(java.util.List.of(
                verticeTalhao(1, "-8.347200000", "-63.871160000", "90.10", "T1"),
                verticeTalhao(2, "-8.348000000", "-63.871190000", "90.20", "T2"),
                verticeTalhao(3, "-8.347500000", "-63.871380000", "90.30", "T3")));

        var salvo = service.salvarTalhao(null, r);
        entityManager.flush();
        entityManager.clear();

        assertThat(salvo.quantidadeVertices()).isEqualTo(3);
        assertThat(salvo.areaGisM2()).isNotNull();
        assertThat(salvo.areaGisM2()).isNotEqualByComparingTo(salvo.areaHa());
        assertThat(jdbc.queryForObject("SELECT geometria_crs_epsg FROM propriedade_talhoes WHERE id=?",
                Integer.class, salvo.id())).isEqualTo(4674);
        assertThat(jdbc.queryForObject("SELECT geometria_geography.STIsValid() FROM propriedade_talhoes WHERE id=?",
                Boolean.class, salvo.id())).isTrue();
        assertThat(jdbc.queryForList("SELECT ordem FROM propriedade_talhao_vertices WHERE talhao_id=? ORDER BY ordem",
                Integer.class, salvo.id())).containsExactly(1, 2, 3);
        assertThat(perimetros.obter().getMapa().talhoes()).extracting(TalhaoMapaResumo::nome)
                .contains("Talhão georreferenciado");
        assertThat(service.geoJsonTalhoes().features().getFirst().geometry().type()).isEqualTo("Polygon");

        var fora = new TalhaoRequest();
        fora.setNome("Talhão fora");
        fora.setAreaHa(new BigDecimal("0.1000"));
        fora.getVertices().addAll(java.util.List.of(
                verticeTalhao(1, "-8.100000000", "-63.100000000", null, "F1"),
                verticeTalhao(2, "-8.100100000", "-63.100000000", null, "F2"),
                verticeTalhao(3, "-8.100000000", "-63.100100000", null, "F3")));
        assertThatThrownBy(() -> service.salvarTalhao(null, fora)).hasMessageContaining("dentro do perímetro");
    }

    @Test @Transactional @org.springframework.security.test.context.support.WithMockUser(username="admin-perimetro",roles="ADMIN")
    void perimetroPersisteOrdemPrecisaoAuditoriaEAtualizaSomenteVertices() {
        var principal = org.mockito.Mockito.mock(com.example.sitiopro.usuario.security.UsuarioPrincipal.class);
        org.mockito.Mockito.when(principal.getUsername()).thenReturn("admin-perimetro");
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null,
                        java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"))));
        var r = perimetros.formulario(); r.setStatusCrs(StatusCrs.NAO_CONFIRMADO); r.setCrs(null); r.setDatum(null);
        r.getVertices().clear();
        r.getVertices().addAll(java.util.List.of(PerimetroServiceTests.vertice(3,"-8.123456789","-63.123456789"),
                PerimetroServiceTests.vertice(1,"-8.2","-63.2"),PerimetroServiceTests.vertice(2,"-8.3","-63.3")));
        var salvo = perimetros.salvar(r); entityManager.clear();
        var lido = perimetros.obter();
        assertThat(lido.vertices()).extracting(PerimetroResumo.Vertice::ordem).containsExactly(1,2,3);
        assertThat(lido.vertices().get(2).latitude()).isEqualByComparingTo("-8.123456789");
        assertThat(lido.statusCrs()).isEqualTo(StatusCrs.NAO_CONFIRMADO);
        assertThat(lido.alteradoPor()).isEqualTo("admin-perimetro");
        assertThat(lido.alteradoEm()).isNotNull();
        var edicao = perimetros.formulario();
        edicao.getVertices().getFirst().setLatitude(new BigDecimal("-8.9"));
        var atualizado = perimetros.salvar(edicao); entityManager.clear();
        assertThat(atualizado.versao()).isGreaterThan(salvo.versao());
        assertThat(perimetros.obter().vertices().getFirst().latitude()).isEqualByComparingTo("-8.9");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM propriedade_perimetro_vertices WHERE perimetro_id=?",Integer.class,salvo.id())).isEqualTo(3);
    }

    @Test @Transactional void perimetroTrocaOrdemRemoveVerticesEConfirmaReferenciaExplicita() {
        var inicial = perimetros.formulario(); inicial.getVertices().clear();
        inicial.setStatusCrs(StatusCrs.NAO_CONFIRMADO); inicial.setCrs(null); inicial.setDatum(null);
        inicial.getVertices().addAll(java.util.List.of(PerimetroServiceTests.vertice(1,"1","2"), PerimetroServiceTests.vertice(2,"3","4")));
        var salvo = perimetros.salvar(inicial);
        var r = perimetros.formulario(); r.getVertices().get(0).setOrdem(2); r.getVertices().get(1).setOrdem(1);
        r.setStatusCrs(StatusCrs.CONFIRMADO); r.setCrs("CRS fornecido no levantamento"); r.setDatum("Datum documentado");
        perimetros.salvar(r); entityManager.clear();
        assertThat(perimetros.obter().vertices().getFirst().latitude()).isEqualByComparingTo("3");
        var limpar = perimetros.formulario(); limpar.getVertices().clear();
        perimetros.salvar(limpar); entityManager.clear();
        assertThat(perimetros.obter().getQuantidadeVertices()).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM propriedade_perimetro_vertices WHERE perimetro_id=?",Integer.class,salvo.id())).isZero();
    }

    @Test @Transactional void versaoAntigaNaoAlteraPerimetro() {
        var r = perimetros.formulario(); r.getVertices().clear();
        r.setStatusCrs(StatusCrs.NAO_CONFIRMADO); r.setCrs(null); r.setDatum(null);
        r.getVertices().add(PerimetroServiceTests.vertice(1,"1","2"));
        var salvo = perimetros.salvar(r);
        assertThatThrownBy(() -> perimetros.salvar(r)).hasMessageContaining("Recarregue");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM propriedade_perimetro_vertices WHERE perimetro_id=?",Integer.class,salvo.id())).isEqualTo(1);
    }

    @ParameterizedTest @ValueSource(strings={"duplicata","ordem","latitude","longitude","fk","crs"})
    @Transactional void constraintsDoPerimetroProtegemSqlDireto(String caso) {
        var salvo = perimetros.obter();
        assertThatThrownBy(() -> {
            switch(caso) {
                case "duplicata" -> jdbc.update("INSERT INTO propriedade_perimetro_vertices(perimetro_id,ordem,latitude,longitude) VALUES (?,2,1,2)",salvo.id());
                case "ordem" -> jdbc.update("INSERT INTO propriedade_perimetro_vertices(perimetro_id,ordem,latitude,longitude) VALUES (?,1,3,4)",salvo.id());
                case "latitude" -> jdbc.update("INSERT INTO propriedade_perimetro_vertices(perimetro_id,ordem,latitude,longitude) VALUES (?,2,91,4)",salvo.id());
                case "longitude" -> jdbc.update("INSERT INTO propriedade_perimetro_vertices(perimetro_id,ordem,latitude,longitude) VALUES (?,2,3,181)",salvo.id());
                case "fk" -> jdbc.update("INSERT INTO propriedade_perimetro_vertices(perimetro_id,ordem,latitude,longitude) VALUES (-999,1,3,4)");
                default -> jdbc.update("UPDATE propriedade_perimetros SET status_crs='CONFIRMADO',crs=NULL WHERE id=?",salvo.id());
            }
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test void v16ValidaEHaUmaPropriedadePrincipalSemDuplicarDadosFisicos() {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1 AND version='16'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM propriedades WHERE principal=1", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM sys.columns WHERE object_id=OBJECT_ID('configuracoes_operacionais') AND name IN ('nome_propriedade','latitude','longitude')", Integer.class)).isZero();
        assertThat(config.obter().nomePropriedade()).isEqualTo(service.resumo().nome());
    }

    @Test @Transactional void configuracoesEPropriedadeCompartilhamFonteDeVerdade() {
        var p = service.formulario(); p.setNome("Nome físico editado");
        p.setLatitudeCentral(new BigDecimal("-8.987654")); p.setLongitudeCentral(new BigDecimal("-63.987654"));
        service.atualizar(p);
        assertThat(config.obter().nomePropriedade()).isEqualTo("Nome físico editado");
        assertThat(config.obter().latitude()).isEqualByComparingTo("-8.987654");
        assertThat(config.obter().revisaoLocalizacao()).isEqualTo(1);
        var admin = config.obter().paraFormulario(); admin.setNomePropriedade("Nome pelo Admin");
        config.atualizar(admin);
        assertThat(service.resumo().nome()).isEqualTo("Nome pelo Admin");
        assertThat(config.obter().revisaoLocalizacao()).isEqualTo(1);
        assertThatThrownBy(() -> service.atualizar(p)).hasMessageContaining("Recarregue");
    }

    @ParameterizedTest @ValueSource(strings = {"areas","talhoes","piquetes","estruturas","recursos"})
    @Transactional void persisteEditaDesativaCadastros(String tipo) {
        var area = service.salvarAreaPropriedade(null, area("Área física"));
        CadastroFisicoResumo criado = switch (tipo) {
            case "areas" -> area;
            case "talhoes" -> {
                var r = new TalhaoRequest(); r.setNome("Talhão"); r.setAreaId(area.id()); r.setAreaHa(new BigDecimal("2.4567"));
                yield service.salvarTalhao(null,r);
            }
            case "piquetes" -> {
                var r = new PiqueteRequest(); r.setNome("Piquete"); r.setAreaId(area.id());
                yield service.salvarPiquete(null,r);
            }
            case "estruturas" -> {
                var r = new EstruturaPropriedadeRequest(); r.setNome("Galpão"); r.setTipo(TipoEstruturaPropriedade.GALPAO);
                r.setAreaId(area.id()); r.setCapacidade(new BigDecimal("100.125")); r.setUnidadeCapacidade("m³");
                yield service.salvarEstruturaPropriedade(null,r);
            }
            default -> {
                var r = new RecursoHidricoRequest(); r.setNome("Poço"); r.setTipo(TipoRecursoHidrico.POCO);
                r.setAreaId(area.id()); r.setCapacidadeLitros(new BigDecimal("25000.500"));
                yield service.salvarRecursoHidrico(null,r);
            }
        };
        assertThat(criado.id()).isPositive();
        assertThat(criado.propriedadeId()).isEqualTo(service.resumo().id());
        if (tipo.equals("talhoes")) assertThat(criado.codigo()).matches("TL-[0-9]{4,}");
        if (tipo.equals("piquetes")) assertThat(criado.codigo()).matches("PQ-[0-9]{4,}");
        CadastroFisicoResumo finalizado = switch (tipo) {
            case "areas" -> {
                var r=service.formularioAreaPropriedade(criado.id()); r.setNome("Área editada");
                service.salvarAreaPropriedade(criado.id(),r);
                var atual=service.detalharAreaPropriedade(criado.id()); service.desativarAreaPropriedade(criado.id(),atual.versao());
                yield service.detalharAreaPropriedade(criado.id());
            }
            case "talhoes" -> { service.desativarTalhao(criado.id(),criado.versao()); yield service.detalharTalhao(criado.id()); }
            case "piquetes" -> { service.desativarPiquete(criado.id(),criado.versao()); yield service.detalharPiquete(criado.id()); }
            case "estruturas" -> { service.desativarEstruturaPropriedade(criado.id(),criado.versao()); yield service.detalharEstruturaPropriedade(criado.id()); }
            default -> { service.desativarRecursoHidrico(criado.id(),criado.versao()); yield service.detalharRecursoHidrico(criado.id()); }
        };
        assertThat(finalizado.ativo()).isFalse();
        assertThat(finalizado.versao()).isGreaterThan(criado.versao());
    }

    @Test @Transactional void instalacoesAntigasSemVinculoContinuamEPermitemVinculoOpcional() {
        var i = new InstalacaoCriacaoRequest(); i.setNome("Instalação antiga"); i.setTipo(TipoInstalacaoCriacao.GALINHEIRO);
        var criada = criacoes.criar(i);
        assertThat(instalacoes.findById(criada.id()).orElseThrow().getEstrutura()).isNull();
        var e = new EstruturaPropriedadeRequest(); e.setNome("Galinheiro físico"); e.setTipo(TipoEstruturaPropriedade.GALINHEIRO);
        var estrutura = service.salvarEstruturaPropriedade(null,e);
        i.setEstruturaId(estrutura.id()); criacoes.atualizar(criada.id(),i);
        assertThat(criacoes.formulario(criada.id()).getEstruturaId()).isEqualTo(estrutura.id());
        assertThat(criacoes.detalhar(criada.id()).estruturaNome()).isEqualTo("Galinheiro físico");
        assertThatThrownBy(() -> service.desativarEstruturaPropriedade(estrutura.id(),estrutura.versao())).hasMessageContaining("Desvincule");
    }

    @Test @Transactional void codigosSaoUnicosEPermanecemNaEdicao() {
        var r = new TalhaoRequest(); r.setNome("Primeiro"); r.setAreaHa(BigDecimal.ONE);
        var primeiro = service.salvarTalhao(null,r);
        r.setNome("Segundo");
        var segundo = service.salvarTalhao(null,r);
        assertThat(primeiro.codigo()).isNotEqualTo(segundo.codigo());
        var editado = service.formularioTalhao(primeiro.id()); editado.setNome("Renomeado");
        assertThat(service.salvarTalhao(primeiro.id(),editado).codigo()).isEqualTo(primeiro.codigo());
    }

    private static VerticeTalhaoRequest verticeTalhao(int ordem, String latitude, String longitude,
            String altitude, String marco) {
        var v = new VerticeTalhaoRequest();
        v.setOrdem(ordem);
        v.setLatitude(new BigDecimal(latitude));
        v.setLongitude(new BigDecimal(longitude));
        if (altitude != null) v.setAltitudeGeodesicaM(new BigDecimal(altitude));
        v.setMarco(marco);
        return v;
    }

    @Test @Transactional void fkCompostaRejeitaAreaDeOutraPropriedade() {
        var a=service.salvarAreaPropriedade(null,area("Área principal"));
        Propriedade outra=new Propriedade(); outra.setNome("Outro imóvel"); propriedades.saveAndFlush(outra);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO propriedade_piquetes (propriedade_id,area_id,nome,status) VALUES (?,?,?,'ATIVO')",
                outra.getId(),a.id(),"Vínculo indevido")).hasStackTraceContaining("fk_propriedade_piquetes_area");
    }

    @Test void optimisticLockingDoSqlRejeitaDuasEdicoesDaMesmaVersao() {
        var criada=service.salvarAreaPropriedade(null,area("Concorrência"));
        try (var em1=emf.createEntityManager(); var em2=emf.createEntityManager()) {
            em1.getTransaction().begin(); em2.getTransaction().begin();
            var a1=em1.find(AreaPropriedade.class,criada.id());
            var a2=em2.find(AreaPropriedade.class,criada.id());
            a1.setNome("Concorrência vencedora"); em1.getTransaction().commit();
            a2.setNome("Concorrência obsoleta");
            assertThatThrownBy(() -> em2.getTransaction().commit()).isInstanceOf(jakarta.persistence.RollbackException.class);
        } finally {
            jdbc.update("DELETE FROM propriedade_areas WHERE id=?",criada.id());
        }
    }

    @Test void upgradeDaV15PreservaValoresEInstalacoes() throws Exception {
        jdbc.execute("CREATE DATABASE propriedade_upgrade");
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName(SQLSERVER.getHost()); ds.setPortNumber(SQLSERVER.getMappedPort(1433));
        ds.setDatabaseName("propriedade_upgrade"); ds.setUser(SQLSERVER.getUsername()); ds.setPassword(SQLSERVER.getPassword());
        ds.setEncrypt("false"); ds.setTrustServerCertificate(true);
        Flyway.configure().dataSource(ds).target("15").load().migrate();
        JdbcTemplate upgrade = new JdbcTemplate(ds);
        upgrade.update("INSERT INTO configuracoes_operacionais (id,nome_propriedade,timezone,latitude,longitude,dias_padrao_incubacao,antecedencia_alerta_eclosao_dias,revisao_localizacao) VALUES (1,?,'America/Porto_Velho',-8.123456,-63.123456,23,4,7)","Imóvel preservado");
        upgrade.update("INSERT INTO criacao_instalacoes (nome,tipo,ativo,versao) VALUES ('Legada','GALINHEIRO',1,0)");
        Flyway.configure().dataSource(ds).load().migrate();
        assertThat(upgrade.queryForObject("SELECT nome FROM propriedades WHERE principal=1",String.class)).isEqualTo("Imóvel preservado");
        assertThat(upgrade.queryForObject("SELECT latitude_central FROM propriedades WHERE principal=1",BigDecimal.class)).isEqualByComparingTo("-8.123456");
        assertThat(upgrade.queryForObject("SELECT longitude_central FROM propriedades WHERE principal=1",BigDecimal.class)).isEqualByComparingTo("-63.123456");
        assertThat(upgrade.queryForObject("SELECT dias_padrao_incubacao FROM configuracoes_operacionais",Integer.class)).isEqualTo(23);
        assertThat(upgrade.queryForObject("SELECT revisao_localizacao FROM configuracoes_operacionais",Long.class)).isEqualTo(7);
        assertThat(upgrade.queryForObject("SELECT COUNT(*) FROM criacao_instalacoes WHERE nome='Legada' AND estrutura_id IS NULL",Integer.class)).isEqualTo(1);
    }

    private AreaPropriedadeRequest area(String nome) {
        var r=new AreaPropriedadeRequest(); r.setNome(nome); r.setTipo(TipoAreaPropriedade.CRIACAO);
        r.setAreaHa(new BigDecimal("4.1234")); return r;
    }
}
