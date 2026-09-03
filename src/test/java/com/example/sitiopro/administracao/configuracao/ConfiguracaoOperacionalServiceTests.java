package com.example.sitiopro.administracao.configuracao;

import com.example.sitiopro.administracao.configuracao.config.ConfiguracaoOperacionalInicialProperties;
import com.example.sitiopro.administracao.configuracao.dto.ConfiguracaoOperacionalForm;
import com.example.sitiopro.administracao.configuracao.entity.ConfiguracaoOperacional;
import com.example.sitiopro.administracao.configuracao.repository.ConfiguracaoOperacionalRepository;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalInvalidaException;
import com.example.sitiopro.administracao.configuracao.service.ConfiguracaoOperacionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import com.example.sitiopro.propriedade.entity.Propriedade;
import com.example.sitiopro.propriedade.service.PropriedadeService;

import static com.example.sitiopro.administracao.configuracao.ConfiguracaoOperacionalTestFixture.padrao;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoOperacionalServiceTests {
    @Mock private ConfiguracaoOperacionalRepository repository;
    @Mock private PropriedadeService propriedadeService;
    private ConfiguracaoOperacionalInicialProperties properties;
    private ConfiguracaoOperacionalService service;

    @BeforeEach
    void preparar() {
        properties = new ConfiguracaoOperacionalInicialProperties();
        service = new ConfiguracaoOperacionalService(repository, properties, propriedadeService);
        lenient().when(propriedadeService.inicializarPrincipal(anyString(), any(), any())).thenAnswer(inv -> {
            Propriedade p = new Propriedade();
            p.setNome(inv.getArgument(0)); p.inicializarCoordenadas(inv.getArgument(1), inv.getArgument(2));
            return p;
        });
    }

    @Test
    void inicializaDefaultsSegurosSemInventarCoordenadas() {
        service.inicializar();
        ArgumentCaptor<ConfiguracaoOperacional> captor = ArgumentCaptor.forClass(ConfiguracaoOperacional.class);
        verify(repository).saveAndFlush(captor.capture());
        var valor = captor.getValue();
        assertThat(valor.getNomePropriedade()).isEqualTo("Sítio Guaratinguetá");
        assertThat(valor.getTimezone()).isEqualTo("Etc/UTC");
        assertThat(valor.getLatitude()).isNull();
        assertThat(valor.getLongitude()).isNull();
        assertThat(valor.getDiasPadraoIncubacao()).isEqualTo(21);
        assertThat(valor.getAntecedenciaAlertaEclosaoDias()).isEqualTo(2);
    }

    @Test
    void importaEnvLegadoApenasNaPrimeiraInicializacao() {
        properties.setTimezone("America/Porto_Velho");
        properties.setLatitude("-8.1234567");
        properties.setLongitude("-63.1234567");
        properties.setDiasPadraoIncubacao(22);
        service.inicializar();
        ArgumentCaptor<ConfiguracaoOperacional> captor = ArgumentCaptor.forClass(ConfiguracaoOperacional.class);
        verify(repository).saveAndFlush(captor.capture());
        var persistida = captor.getValue();
        assertThat(persistida.getLatitude()).isEqualByComparingTo("-8.123457");
        assertThat(persistida.getTimezone()).isEqualTo("America/Porto_Velho");
        assertThat(persistida.getDiasPadraoIncubacao()).isEqualTo(22);
        when(repository.buscarParaInicializar()).thenReturn(Optional.of(persistida));
        properties.setDiasPadraoIncubacao(99);
        properties.setTimezone("Asia/Tokyo");
        service.inicializar();
        verify(repository, times(1)).saveAndFlush(any());
        assertThat(persistida.getDiasPadraoIncubacao()).isEqualTo(22);
        assertThat(persistida.getTimezone()).isEqualTo("America/Porto_Velho");
    }

    @Test
    void defaultsInvalidosDeDeploymentNaoCriamConfiguracaoInsegura() {
        properties.setNomePropriedade(" ");
        properties.setTimezone("fuso-invalido");
        properties.setLatitude("91");
        properties.setLongitude("-60");
        properties.setDiasPadraoIncubacao(-1);
        properties.setAntecedenciaAlertaEclosaoDias(99);
        service.inicializar();
        verify(repository).saveAndFlush(argThat(c -> c.getLatitude() == null && c.getLongitude() == null
                && c.getDiasPadraoIncubacao() == 21 && c.getAntecedenciaAlertaEclosaoDias() == 2
                && c.getTimezone().equals("Etc/UTC")));
    }

    @Test
    void obterLeSomenteSqlSemRegravarOuConsultarDefaults() {
        when(repository.findById(1)).thenReturn(Optional.of(entidade()));
        properties.setTimezone("invalido");
        assertThat(service.obter().timezone()).isEqualTo("UTC");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void atualizarNormalizaNomeEIsolaLocalizacaoAnteriorSemMexerEmIncubacoes() {
        var entidade = entidade();
        when(repository.findById(1)).thenReturn(Optional.of(entidade));
        doAnswer(inv -> {
            entidade.getPropriedade().setNome(inv.getArgument(0));
            entidade.getPropriedade().atualizarCoordenadas(inv.getArgument(1), inv.getArgument(2));
            return null;
        }).when(propriedadeService).atualizarDadosFisicos(anyString(), any(), any(), any());
        when(repository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        var form = padrao().paraFormulario();
        form.setNomePropriedade("  Meu Sítio  ");
        form.setLatitude(new BigDecimal("-8"));
        var resultado = service.atualizar(form);
        assertThat(resultado.nomePropriedade()).isEqualTo("Meu Sítio");
        assertThat(resultado.revisaoLocalizacao()).isEqualTo(1);
        assertThat(resultado.contextoClima("principal")).isEqualTo("principal-local-1");
    }

    @Test
    void alteracaoDeNomeOuEscalaDecimalNaoMudaRevisaoLocalizacao() {
        when(repository.findById(1)).thenReturn(Optional.of(entidade()));
        when(repository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
        var form = padrao().paraFormulario();
        form.setLatitude(new BigDecimal("-3.000000"));
        form.setNomePropriedade("Outro nome");
        assertThat(service.atualizar(form).revisaoLocalizacao()).isZero();
    }

    @ParameterizedTest
    @ValueSource(strings = {"timezone-invalido", "", "GMT+25", "+03:00"})
    void rejeitaTimezoneInvalido(String timezone) {
        var form = padrao().paraFormulario();
        form.setTimezone(timezone);
        rejeita(form, "timezone");
    }

    @ParameterizedTest
    @CsvSource({"91,-60,latitude", "-91,-60,latitude", "-3,181,longitude", "-3,-181,longitude",
            "-3.1234567,-60,latitude", "-3,-60.1234567,longitude"})
    void rejeitaCoordenadasInvalidas(String lat, String lon, String campo) {
        var form = padrao().paraFormulario();
        form.setLatitude(new BigDecimal(lat));
        form.setLongitude(new BigDecimal(lon));
        rejeita(form, campo);
    }

    @Test
    void rejeitaCoordenadaIsolada() {
        var form = padrao().paraFormulario();
        form.setLatitude(null);
        rejeita(form, "latitude");
    }

    @Test
    void rejeitaAntecedenciaMaiorQueIncubacao() {
        var form = padrao().paraFormulario();
        form.setDiasPadraoIncubacao(3);
        form.setAntecedenciaAlertaEclosaoDias(4);
        rejeita(form, "antecedenciaAlertaEclosaoDias");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 121})
    void rejeitaPeriodoInvalido(int dias) {
        var form = padrao().paraFormulario();
        form.setDiasPadraoIncubacao(dias);
        rejeita(form, "diasPadraoIncubacao");
    }

    private void rejeita(ConfiguracaoOperacionalForm form, String campo) {
        assertThatThrownBy(() -> service.atualizar(form))
                .isInstanceOfSatisfying(ConfiguracaoOperacionalInvalidaException.class,
                        ex -> assertThat(ex.getCampo()).isEqualTo(campo));
        verifyNoInteractions(repository);
    }

    private ConfiguracaoOperacional entidade() {
        Propriedade p = new Propriedade();
        p.setNome("Teste"); p.inicializarCoordenadas(new BigDecimal("-3"), new BigDecimal("-60"));
        return new ConfiguracaoOperacional(p, "UTC", 21, 2);
    }
}
