package com.example.sitiopro.frota.service;

import com.example.sitiopro.frota.model.FipeCache;
import com.example.sitiopro.frota.repository.FipeCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class FipeServiceTests {

    @Test
    void preservaPrecisaoDoValorBrasileiroSemExporCache() {
        FipeCacheRepository repository = mock(FipeCacheRepository.class);
        FipeService service = new FipeService(repository, new ObjectMapper(), new RestTemplateBuilder());
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        when(repository.findById(123)).thenReturn(Optional.empty());
        when(repository.save(any(FipeCache.class))).thenAnswer(invocation -> invocation.getArgument(0));
        server.expect(requestTo("https://api.fipex.com.br/v1/veiculos/123"))
                .andRespond(withSuccess("""
                        {"valor":"R$ 123.456,78","ano_modelo":"2024","historico":[]}
                        """, MediaType.APPLICATION_JSON));

        var resposta = service.buscarDetalhes(123);

        assertThat(resposta.valor()).isEqualByComparingTo(new BigDecimal("123456.78"));
        server.verify();
    }

    @Test
    void indisponibilidadeRemotaUsaErroPadronizado() {
        FipeCacheRepository repository = mock(FipeCacheRepository.class);
        FipeService service = new FipeService(repository, new ObjectMapper(), new RestTemplateBuilder());
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(requestTo("https://api.fipex.com.br/v1/marcas?tipo_veiculo_id=1"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> service.buscarMarcas(1))
                .isInstanceOfSatisfying(FrotaOperacaoException.class, ex -> {
                    assertThat(ex.getCodigo()).isEqualTo("FIPE_INDISPONIVEL");
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
        server.verify();
    }
}
