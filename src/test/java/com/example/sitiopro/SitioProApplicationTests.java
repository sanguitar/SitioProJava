package com.example.sitiopro;

import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.categoria.repository.CategoriaRepository;
import com.example.sitiopro.estoque.repository.CategoriaEstoqueRepository;
import com.example.sitiopro.estoque.repository.ItemEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.LoteEstoqueRepository;
import com.example.sitiopro.estoque.repository.MovimentoEstoqueRepository;
import com.example.sitiopro.estoque.repository.UnidadeMedidaRepository;
import com.example.sitiopro.frota.repository.FipeCacheRepository;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;

@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class SitioProApplicationTests {

    @MockBean
    private AbastecimentoRepository abastecimentoRepository;

    @MockBean
    private CategoriaRepository categoriaRepository;

    @MockBean
    private FipeCacheRepository fipeCacheRepository;

    @MockBean
    private ProducaoRepository producaoRepository;

    @MockBean
    private VeiculoRepository veiculoRepository;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private CategoriaEstoqueRepository estoqueCategoriaRepository;

    @MockBean
    private UnidadeMedidaRepository unidadeMedidaRepository;

    @MockBean
    private LocalEstoqueRepository localEstoqueRepository;

    @MockBean
    private ItemEstoqueRepository itemEstoqueRepository;

    @MockBean
    private LoteEstoqueRepository loteEstoqueRepository;

    @MockBean
    private MovimentoEstoqueRepository movimentoEstoqueRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void contextLoads() {
    }
}
