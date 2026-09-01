package com.example.sitiopro.usuario.integration;

import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.usuario.dto.CriarUsuarioRequest;
import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import com.example.sitiopro.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

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
class UsuarioSqlServerIntegrationTests {

    @Container
    static final MSSQLServerContainer<?> SQLSERVER =
            new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();

    @DynamicPropertySource
    static void sqlServerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", SQLSERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQLSERVER::getUsername);
        registry.add("spring.datasource.password", SQLSERVER::getPassword);
        registry.add("spring.flyway.user", SQLSERVER::getUsername);
        registry.add("spring.flyway.password", SQLSERVER::getPassword);
    }

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;

    @Test
    void desativacoesConcorrentesPreservamUmAdministradorAtivo() throws Exception {
        Usuario adminA = usuarioService.criar(request("admin-a-" + System.nanoTime()));
        Usuario adminB = usuarioService.criar(request("admin-b-" + System.nanoTime()));

        List<Boolean> resultados = executarEmParalelo(adminA.getId(), adminB.getId());

        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        assertThat(usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN)).isEqualTo(1);
    }

    private CriarUsuarioRequest request(String login) {
        CriarUsuarioRequest request = new CriarUsuarioRequest();
        request.setNome("Administrador de auditoria");
        request.setLogin(login);
        request.setSenhaInicial("SenhaMuitoForte123!");
        request.setPerfil(PerfilUsuario.ADMIN);
        request.setAtivo(true);
        return request;
    }

    private List<Boolean> executarEmParalelo(Long primeiroId, Long segundoId) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futuros = new ArrayList<>();
            for (Long id : List.of(primeiroId, segundoId)) {
                futuros.add(executor.submit(() -> {
                    inicio.await();
                    try {
                        usuarioService.desativar(id);
                        return true;
                    } catch (RuntimeException ex) {
                        return false;
                    }
                }));
            }
            inicio.countDown();
            List<Boolean> resultados = new ArrayList<>();
            for (Future<Boolean> futuro : futuros) {
                resultados.add(futuro.get());
            }
            return resultados;
        } finally {
            executor.shutdownNow();
        }
    }
}
