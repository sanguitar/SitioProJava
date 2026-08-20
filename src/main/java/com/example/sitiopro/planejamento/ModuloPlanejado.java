package com.example.sitiopro.planejamento;

import java.util.List;

public record ModuloPlanejado(
        String grupo,
        String titulo,
        String basePath,
        String active,
        String cssFile,
        String domainClass,
        String icon,
        StatusPlanejamento roadmapStatus,
        String descricao,
        List<String> funcionalidades,
        List<LinkAtalho> atalhos,
        List<AcaoModulo> acoes) {
}
