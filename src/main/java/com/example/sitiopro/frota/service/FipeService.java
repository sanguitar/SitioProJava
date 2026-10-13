package com.example.sitiopro.frota.service;

import com.example.sitiopro.frota.dto.*;
import com.example.sitiopro.frota.model.FipeCache;
import com.example.sitiopro.frota.repository.FipeCacheRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

@Service
public class FipeService {
    private static final String BASE_URL = "https://api.fipex.com.br/v1";
    private final FipeCacheRepository cacheRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public FipeService(FipeCacheRepository cacheRepository, ObjectMapper objectMapper,
            RestTemplateBuilder restTemplateBuilder) {
        this.cacheRepository = cacheRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8)).build();
    }

    public List<FipeOpcaoResponse> buscarMarcas(Integer tipo) {
        MarcaDTO[] response = consultarRemoto(
                BASE_URL + "/marcas?tipo_veiculo_id=" + tipo, MarcaDTO[].class);
        return Arrays.stream(response != null ? response : new MarcaDTO[0])
                .map(item -> new FipeOpcaoResponse(item.getId(), item.getNome())).toList();
    }

    public List<FipeOpcaoResponse> buscarModelos(Integer tipo, Integer marca) {
        String url = String.format("%s/modelos?tipo_veiculo_id=%d&marca_id=%d", BASE_URL, tipo, marca);
        MarcaDTO[] response = consultarRemoto(url, MarcaDTO[].class);
        return Arrays.stream(response != null ? response : new MarcaDTO[0])
                .map(item -> new FipeOpcaoResponse(item.getId(), item.getNome())).toList();
    }

    public List<FipeAnoResponse> buscarAnos(Integer tipo, Integer marca, Integer modelo) {
        String url = String.format("%s/veiculos?tipo_veiculo_id=%d&marca_id=%d&modelo_id=%d",
                BASE_URL, tipo, marca, modelo);
        JsonNode response = consultarRemoto(url, JsonNode.class);
        if (response == null || !response.isArray()) return List.of();
        List<FipeAnoResponse> anos = new ArrayList<>();
        response.forEach(item -> anos.add(new FipeAnoResponse(
                item.path("id").canConvertToInt() ? item.path("id").intValue() : null,
                item.path("ano_modelo").asText(null), item.path("combustivel").asText(null))));
        return List.copyOf(anos);
    }

    public FipeDetalheResponse buscarDetalhes(Integer id) {
        FipeCache cache = cacheRepository.findById(id).orElse(null);
        if (cache != null) return resposta(cache);
        FipeCache consultado = consultarApi(id);
        if (consultado == null) return null;
        try {
            return resposta(cacheRepository.save(consultado));
        } catch (DataIntegrityViolationException ex) {
            return resposta(cacheRepository.findById(id).orElseThrow(() -> ex));
        }
    }

    private FipeCache consultarApi(Integer id) {
        try {
            VeiculoFipeDTO dto = consultarRemoto(BASE_URL + "/veiculos/" + id, VeiculoFipeDTO.class);
            if (dto == null) return null;
            FipeCache novo = new FipeCache();
            novo.setId(id); novo.setAnoModelo(dto.getAno_modelo()); novo.setValor(parseValor(dto.getValor()));
            novo.setHistoricoJson(objectMapper.writeValueAsString(dto.getHistorico()));
            return novo;
        } catch (JsonProcessingException | NumberFormatException ex) {
            throw new FrotaOperacaoException("FIPE_RESPOSTA_INVALIDA",
                    "A FIPE retornou dados inválidos.", HttpStatus.BAD_GATEWAY);
        }
    }

    private <T> T consultarRemoto(String url, Class<T> responseType) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (HttpClientErrorException.NotFound ex) {
            return null;
        } catch (RestClientException ex) {
            throw new FrotaOperacaoException("FIPE_INDISPONIVEL",
                    "A consulta FIPE está temporariamente indisponível.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private BigDecimal parseValor(String valor) {
        if (valor == null) return null;
        String valorLimpo = valor.replaceAll("[^0-9,.]", "").replace(".", "").replace(",", ".");
        if (valorLimpo.isBlank()) return null;
        return new BigDecimal(valorLimpo).setScale(2, RoundingMode.HALF_UP);
    }

    private FipeDetalheResponse resposta(FipeCache cache) {
        try {
            JsonNode historico = cache.getHistoricoJson() == null
                    ? objectMapper.createArrayNode() : objectMapper.readTree(cache.getHistoricoJson());
            return new FipeDetalheResponse(cache.getId(), cache.getAnoModelo(), cache.getValor(), historico);
        } catch (JsonProcessingException ex) {
            return new FipeDetalheResponse(cache.getId(), cache.getAnoModelo(), cache.getValor(), objectMapper.createArrayNode());
        }
    }
}
