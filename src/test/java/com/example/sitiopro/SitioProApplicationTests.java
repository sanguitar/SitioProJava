package com.example.sitiopro;

import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import com.example.sitiopro.abastecimento.repository.AbastecimentoRepository;
import com.example.sitiopro.categoria.repository.CategoriaRepository;
import com.example.sitiopro.compras.repository.CompraRepository;
import com.example.sitiopro.compras.repository.FornecedorRepository;
import com.example.sitiopro.criacao.aves.repository.AcompanhamentoIncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.AlimentacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.EventoLoteAvesRepository;
import com.example.sitiopro.criacao.aves.repository.IncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.LoteAvesRepository;
import com.example.sitiopro.criacao.aves.repository.MortalidadeAvesRepository;
import com.example.sitiopro.criacao.aves.repository.OvoIncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.OvoscopiaIncubacaoAvesRepository;
import com.example.sitiopro.criacao.aves.repository.PesagemAvesRepository;
import com.example.sitiopro.criacao.aves.repository.RegistroPosturaAvesRepository;
import com.example.sitiopro.criacao.aves.repository.TransferenciaLoteAvesRepository;
import com.example.sitiopro.criacao.core.repository.InstalacaoCriacaoRepository;
import com.example.sitiopro.criacao.core.service.CodigoCriacaoService;
import com.example.sitiopro.estoque.repository.CategoriaEstoqueRepository;
import com.example.sitiopro.estoque.repository.ItemEstoqueRepository;
import com.example.sitiopro.estoque.repository.LocalEstoqueRepository;
import com.example.sitiopro.estoque.repository.LoteEstoqueRepository;
import com.example.sitiopro.estoque.repository.MovimentoEstoqueRepository;
import com.example.sitiopro.estoque.repository.UnidadeMedidaRepository;
import com.example.sitiopro.frota.repository.FipeCacheRepository;
import com.example.sitiopro.frota.repository.VeiculoRepository;
import com.example.sitiopro.integracao.clima.repository.PrevisaoClimaticaRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoEstadoRepository;
import com.example.sitiopro.integracao.core.repository.IntegracaoExecucaoRepository;
import com.example.sitiopro.integracao.embrapa.agrofit.repository.AgrofitCulturaRepository;
import com.example.sitiopro.observability.service.SistemaSaudeService;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import com.example.sitiopro.propriedade.repository.PerimetroSpatialRepository;
import com.example.sitiopro.propriedade.repository.TalhaoSpatialRepository;
import com.example.sitiopro.tarefas.repository.AlertaRepository;
import com.example.sitiopro.tarefas.repository.EventoTarefaAlertaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRecorrenciaRepository;
import com.example.sitiopro.tarefas.repository.TarefaRepository;
import com.example.sitiopro.tarefas.service.SqlServerApplicationLock;
import com.example.sitiopro.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.web.client.RestClient;

@MockBean(name = "openMeteoRestClient", classes = RestClient.class)
@MockBean(name = "agrofitRestClient", classes = RestClient.class)
@MockBean(classes = SistemaSaudeService.class)
@SpringBootTest(properties = {
        "spring.profiles.active=test",
        "sitiopro.tarefas.scheduler-enabled=false",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
})
class SitioProApplicationTests {
    @MockBean private com.example.sitiopro.agricultura.service.AgriculturaService agriculturaService;
    @MockBean private com.example.sitiopro.propriedade.service.PropriedadeService propriedadeService;
    @MockBean private com.example.sitiopro.propriedade.service.PerimetroService perimetroService;
    @MockBean private PerimetroSpatialRepository perimetroSpatialRepository;
    @MockBean private TalhaoSpatialRepository talhaoSpatialRepository;
    @MockBean
    private ConfiguracaoOperacionalService configuracaoOperacionalService;

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
    private CompraRepository compraRepository;

    @MockBean
    private FornecedorRepository fornecedorRepository;

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
    private InstalacaoCriacaoRepository instalacaoCriacaoRepository;

    @MockBean
    private LoteAvesRepository loteAvesRepository;

    @MockBean
    private EventoLoteAvesRepository eventoLoteAvesRepository;

    @MockBean
    private MortalidadeAvesRepository mortalidadeAvesRepository;

    @MockBean
    private AlimentacaoAvesRepository alimentacaoAvesRepository;

    @MockBean
    private PesagemAvesRepository pesagemAvesRepository;

    @MockBean
    private RegistroPosturaAvesRepository registroPosturaAvesRepository;

    @MockBean
    private TransferenciaLoteAvesRepository transferenciaLoteAvesRepository;

    @MockBean
    private IncubacaoAvesRepository incubacaoAvesRepository;

    @MockBean
    private AcompanhamentoIncubacaoAvesRepository acompanhamentoIncubacaoAvesRepository;

    @MockBean
    private OvoIncubacaoAvesRepository ovoIncubacaoAvesRepository;

    @MockBean
    private OvoscopiaIncubacaoAvesRepository ovoscopiaIncubacaoAvesRepository;

    @MockBean
    private IntegracaoEstadoRepository integracaoEstadoRepository;

    @MockBean
    private IntegracaoExecucaoRepository integracaoExecucaoRepository;

    @MockBean
    private PrevisaoClimaticaRepository previsaoClimaticaRepository;

    @MockBean
    private AgrofitCulturaRepository agrofitCulturaRepository;

    @MockBean
    private TarefaRepository tarefaRepository;

    @MockBean
    private TarefaRecorrenciaRepository tarefaRecorrenciaRepository;

    @MockBean
    private AlertaRepository alertaRepository;

    @MockBean
    private EventoTarefaAlertaRepository eventoTarefaAlertaRepository;

    @MockBean
    private SqlServerApplicationLock sqlServerApplicationLock;

    @MockBean
    private CodigoCriacaoService codigoCriacaoService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void contextLoads() {
    }
}
