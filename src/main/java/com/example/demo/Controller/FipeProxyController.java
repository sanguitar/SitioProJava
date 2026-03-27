package com.example.demo.Controller;

import com.example.demo.DTO.MarcaDTO;
import com.example.demo.DTO.VeiculoFipeDTO;
import com.example.demo.Model.FipeCache;
import com.example.demo.Repository.FipeCacheRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fipe")
public class FipeProxyController {

    @Autowired
    private FipeCacheRepository cacheRepository;

    private final String BASE_URL = "https://api.fipex.com.br/v1";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/marcas")
    public List<MarcaDTO> getMarcas(@RequestParam Integer tipo) {
        String url = BASE_URL + "/marcas?tipo_veiculo_id=" + tipo;
        MarcaDTO[] res = restTemplate.getForObject(url, MarcaDTO[].class);
        return Arrays.asList(res != null ? res : new MarcaDTO[0]);
    }

    @GetMapping("/modelos")
    public List<MarcaDTO> getModelos(@RequestParam Integer tipo, @RequestParam Integer marca) {
        String url = String.format("%s/modelos?tipo_veiculo_id=%d&marca_id=%d", BASE_URL, tipo, marca);
        MarcaDTO[] res = restTemplate.getForObject(url, MarcaDTO[].class);
        return Arrays.asList(res != null ? res : new MarcaDTO[0]);
    }

    @GetMapping("/anos")
    public List<Object> getAnos(@RequestParam Integer tipo, @RequestParam Integer marca, @RequestParam Integer modelo) {
        String url = String.format("%s/veiculos?tipo_veiculo_id=%d&marca_id=%d&modelo_id=%d", BASE_URL, tipo, marca,
                modelo);
        Object[] res = restTemplate.getForObject(url, Object[].class);
        return Arrays.asList(res != null ? res : new Object[0]);
    }

    // AQUI É ONDE O CACHE ACONTECE
    @GetMapping("/detalhes/{id}")
    public Object getDetalhes(@PathVariable Integer id) {
        // 1. Tenta buscar no Banco de Dados do Sítio
        Optional<FipeCache> cache = cacheRepository.findById(id);
        if (cache.isPresent()) {
            return cache.get(); // Retorno instantâneo!
        }

        // 2. Se não achou, vai na API, salva para a próxima vez e retorna
        try {
            VeiculoFipeDTO dto = restTemplate.getForObject(BASE_URL + "/veiculos/" + id, VeiculoFipeDTO.class);
            if (dto != null) {
                FipeCache novo = new FipeCache();
                novo.setId(id);
                // Converte String "R$ 50.000" para Double 50000.0
                String valorLimpo = dto.getValor().replaceAll("[^0-9,]", "").replace(",", ".");
                novo.setValor(Double.parseDouble(valorLimpo));

                // Salva o histórico como JSON para o gráfico de prédios
                novo.setHistoricoJson(objectMapper.writeValueAsString(dto.getHistorico()));

                return cacheRepository.save(novo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}