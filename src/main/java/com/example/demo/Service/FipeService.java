package com.example.demo.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.DTO.MarcaDTO;

import java.util.Arrays;
import java.util.List;

@Service
public class FipeService {
    private final String API_URL = "https://api.fipex.com.br/v1";

    public List<MarcaDTO> buscarMarcas(Integer tipoVeiculoId) {
        RestTemplate restTemplate = new RestTemplate();
        // O Java faz a chamada pesada e entrega o JSON pronto para o Controller
        MarcaDTO[] marcas = restTemplate.getForObject(
                API_URL + "/marcas?tipo_veiculo_id=" + tipoVeiculoId,
                MarcaDTO[].class);
        return Arrays.asList(marcas);
    }
}