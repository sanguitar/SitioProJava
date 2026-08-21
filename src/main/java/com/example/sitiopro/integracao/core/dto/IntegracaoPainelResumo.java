package com.example.sitiopro.integracao.core.dto;

import java.util.List;
import java.util.Map;

public record IntegracaoPainelResumo(
        long operacionais,
        long comAtencao,
        long naoConfiguradas,
        long falhas,
        Map<String, List<IntegracaoFonteResumo>> fontesPorGrupo) {
}
