package com.example.sitiopro.dashboard.dto;

import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.integracao.clima.dto.ClimaResumo;
import com.example.sitiopro.producao.model.Producao;
import org.springframework.data.domain.Page;

import java.util.List;

public record DashboardResumo(
        Page<Producao> paginaEstoque,
        List<Categoria> categorias,
        String labelsGraficoJson,
        String dadosGraficoJson,
        long totalItens,
        int totalCategorias,
        long itensAlerta,
        ClimaResumo clima) {
}
