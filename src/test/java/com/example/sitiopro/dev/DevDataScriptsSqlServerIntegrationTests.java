package com.example.sitiopro.dev;

import com.example.sitiopro.compras.service.FornecedorService;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@MockBean(name = "openMeteoRestClient", classes = RestClient.class)
@MockBean(name = "agrofitRestClient", classes = RestClient.class)
@MockBean(classes = SistemaSaudeService.class)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
        "sitiopro.initial-admin.enabled=false"
})
class DevDataScriptsSqlServerIntegrationTests {

    private static final Path RESET_SCRIPT = Path.of("scripts", "dev", "reset-dev-data.sql");
    private static final Path SEED_SCRIPT = Path.of("scripts", "dev", "seed-dev-data.sql");

    @Container
    static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest")
                    .acceptLicense();

    @DynamicPropertySource
    static void sqlServerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQLSERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQLSERVER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER::getPassword);
        registry.add("spring.flyway.user", SQLSERVER::getUsername);
        registry.add("spring.flyway.password", SQLSERVER::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EstoqueCatalogoService estoqueCatalogoService;

    @Autowired
    private FornecedorService fornecedorService;

    @BeforeEach
    void garantirAdministradorPrincipal() {
        jdbcTemplate.update("""
                IF NOT EXISTS (SELECT 1 FROM dbo.usuarios WHERE login = 'sanderson')
                BEGIN
                    INSERT INTO dbo.usuarios (
                        nome, login, senha_hash, perfil, ativo, criado_em, criado_por
                    ) VALUES (
                        'Sanderson DEV', 'sanderson', '{noop}senha-ficticia-de-teste',
                        'ADMIN', 1, SYSUTCDATETIME(), 'test-fixture'
                    );
                END
                """);
    }

    @Test
    void resetRecusaConfirmacaoInvalidaAntesDeAlterarDados() throws IOException {
        Integer categoriasAntes = count("dbo.estoque_categorias");
        Integer flywayAntes = count("dbo.flyway_schema_history");

        assertThatThrownBy(() -> executarReset("NOT_CONFIRMED"))
                .hasMessageContaining("confirmacao explicita ausente");

        assertThat(count("dbo.estoque_categorias")).isEqualTo(categoriasAntes);
        assertThat(count("dbo.flyway_schema_history")).isEqualTo(flywayAntes);
        assertThat(adminAtivo()).isEqualTo(1);
    }

    @Test
    void resetESeedSaoSegurosIdempotentesECompativeisComCompras() throws IOException {
        jdbcTemplate.update("""
                INSERT INTO dbo.usuarios (
                    nome, login, senha_hash, perfil, ativo, criado_em, criado_por
                ) VALUES (
                    'Usuário temporário', 'usuario-temporario', '{noop}temporaria',
                    'OPERADOR', 1, SYSUTCDATETIME(), 'test-fixture'
                )
                """);
        jdbcTemplate.update("""
                INSERT INTO dbo.producao (item, quantidade, unidade, status, criado_por)
                VALUES ('Smoke legado', 1, 'UN', 'Estoque', 'test-fixture')
                """);
        jdbcTemplate.update("""
                INSERT INTO dbo.fornecedores (nome, ativo, criado_em, criado_por)
                VALUES ('Fornecedor Smoke', 1, SYSUTCDATETIME(), 'test-fixture')
                """);

        String hashAntes = jdbcTemplate.queryForObject(
                "SELECT senha_hash FROM dbo.usuarios WHERE login = 'sanderson'", String.class);
        Integer flywayAntes = count("dbo.flyway_schema_history");

        executarReset("LOCAL_DEV_RESET_CONFIRMED");

        assertThat(count("dbo.usuarios")).isEqualTo(1);
        assertThat(adminAtivo()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT senha_hash FROM dbo.usuarios WHERE login = 'sanderson'", String.class))
                .isEqualTo(hashAntes);
        assertThat(count("dbo.flyway_schema_history")).isEqualTo(flywayAntes);
        assertThat(count("dbo.producao")).isZero();
        assertThat(count("dbo.categorias")).isZero();

        executarSeed();
        executarSeed();

        assertThat(count("dbo.estoque_categorias")).isEqualTo(22);
        assertThat(count("dbo.estoque_unidades_medida")).isEqualTo(12);
        assertThat(count("dbo.estoque_locais")).isEqualTo(13);
        assertThat(count("dbo.estoque_itens")).isEqualTo(145);
        assertThat(count("dbo.fornecedores")).isEqualTo(8);
        assertThat(count("dbo.estoque_movimentos")).isZero();
        assertThat(count("dbo.compras")).isZero();
        assertThat(count("dbo.itens_compra")).isZero();

        assertThat(duplicados("dbo.estoque_categorias", "nome")).isZero();
        assertThat(duplicados("dbo.estoque_unidades_medida", "sigla")).isZero();
        assertThat(duplicados("dbo.estoque_itens", "nome")).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.estoque_itens WHERE ativo = 0", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.fornecedores WHERE ativo = 0", Integer.class)).isZero();

        List<String> itensDisponiveisEmCompras = estoqueCatalogoService.listarItensAtivos().stream()
                .map(item -> item.getNome())
                .toList();
        assertThat(itensDisponiveisEmCompras)
                .contains("Arroz", "Feijão carioca", "Milho em grão", "Ração postura");
        assertThat(fornecedorService.listarAtivos())
                .extracting("nome")
                .contains("Casa das Rações");
    }

    private void executarReset(String confirmation) throws IOException {
        String script = carregar(RESET_SCRIPT)
                .replace("$(DevResetConfirmation)", confirmation)
                .replace("$(ExpectedDatabase)", bancoAtual());
        jdbcTemplate.execute(script);
    }

    private void executarSeed() throws IOException {
        String script = carregar(SEED_SCRIPT)
                .replace("$(ExpectedDatabase)", bancoAtual());
        jdbcTemplate.execute(script);
    }

    private String carregar(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private Integer count(String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    private Integer duplicados(String table, String column) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (SELECT " + column
                + " FROM " + table + " GROUP BY " + column + " HAVING COUNT(*) > 1) duplicados", Integer.class);
    }

    private Integer adminAtivo() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dbo.usuarios
                WHERE login = 'sanderson' AND perfil = 'ADMIN' AND ativo = 1
                """, Integer.class);
    }

    private String bancoAtual() {
        return jdbcTemplate.queryForObject("SELECT DB_NAME()", String.class);
    }
}
