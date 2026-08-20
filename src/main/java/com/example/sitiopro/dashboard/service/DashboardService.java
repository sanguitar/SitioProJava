package com.example.sitiopro.dashboard.service;

import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.service.CategoriaService;
import com.example.sitiopro.dashboard.dto.DashboardResumo;
import com.example.sitiopro.producao.service.ProducaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final CategoriaService categoriaService;
    private final ProducaoService producaoService;
    private final ObjectMapper objectMapper;

    public DashboardService(CategoriaService categoriaService, ProducaoService producaoService,
            ObjectMapper objectMapper) {
        this.categoriaService = categoriaService;
        this.producaoService = producaoService;
        this.objectMapper = objectMapper;
    }

    public DashboardResumo montarResumo(Long categoriaId, int page) {
        List<Categoria> categorias = categoriaService.listarTodas();
        List<String> labels = categorias.stream().map(Categoria::getNome).toList();
        List<Long> dados = categorias.stream().map(producaoService::contarPorCategoria).toList();

        return new DashboardResumo(
                producaoService.listarPaginado(categoriaId, page, 10),
                categorias,
                toJson(labels),
                toJson(dados),
                producaoService.contarTodos(),
                categorias.size(),
                producaoService.contarAlertas());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Não foi possível serializar os dados do gráfico", e);
        }
    }
}
