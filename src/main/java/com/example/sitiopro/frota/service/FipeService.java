package com.example.sitiopro.frota.service;

import com.example.sitiopro.frota.dto.MarcaDTO;
import com.example.sitiopro.frota.dto.VeiculoFipeDTO;
import com.example.sitiopro.frota.model.FipeCache;
import com.example.sitiopro.frota.repository.FipeCacheRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class FipeService {

    private static final String BASE_URL = "https://api.fipex.com.br/v1";

    private final FipeCacheRepository cacheRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public FipeService(FipeCacheRepository cacheRepository, ObjectMapper objectMapper) {
        this.cacheRepository = cacheRepository;
        this.objectMapper = objectMapper;
    }

    public List<MarcaDTO> buscarMarcas(Integer tipo) {
        String url = BASE_URL + "/marcas?tipo_veiculo_id=" + tipo;
        MarcaDTO[] response = restTemplate.getForObject(url, MarcaDTO[].class);
        return Arrays.asList(response != null ? response : new MarcaDTO[0]);
    }

    public List<MarcaDTO> buscarModelos(Integer tipo, Integer marca) {
        String url = String.format("%s/modelos?tipo_veiculo_id=%d&marca_id=%d", BASE_URL, tipo, marca);
        MarcaDTO[] response = restTemplate.getForObject(url, MarcaDTO[].class);
        return Arrays.asList(response != null ? response : new MarcaDTO[0]);
    }

    public List<Object> buscarAnos(Integer tipo, Integer marca, Integer modelo) {
        String url = String.format("%s/veiculos?tipo_veiculo_id=%d&marca_id=%d&modelo_id=%d", BASE_URL, tipo, marca,
                modelo);
        Object[] response = restTemplate.getForObject(url, Object[].class);
        return Arrays.asList(response != null ? response : new Object[0]);
    }

    @Transactional
    public FipeCache buscarDetalhes(Integer id) {
        return cacheRepository.findById(id).orElseGet(() -> consultarApiESalvarCache(id));
    }

    private FipeCache consultarApiESalvarCache(Integer id) {
        try {
            VeiculoFipeDTO dto = restTemplate.getForObject(BASE_URL + "/veiculos/" + id, VeiculoFipeDTO.class);
            if (dto == null) {
                return null;
            }

            FipeCache novo = new FipeCache();
            novo.setId(id);
            novo.setAnoModelo(dto.getAno_modelo());
            novo.setValor(parseValor(dto.getValor()));
            novo.setHistoricoJson(toJson(dto.getHistorico()));
            return cacheRepository.save(novo);
        } catch (RuntimeException | JsonProcessingException e) {
            return null;
        }
    }

    private Double parseValor(String valor) {
        if (valor == null) {
            return null;
        }
        String valorLimpo = valor.replaceAll("[^0-9,]", "").replace(",", ".");
        if (valorLimpo.isBlank()) {
            return null;
        }
        return Double.parseDouble(valorLimpo);
    }

    private String toJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }
}
