package com.example.sitiopro.agricultura.dto;

import com.example.sitiopro.agricultura.entity.*;
import java.time.LocalDateTime;

public record OcorrenciaHistoricoResumo(Long id, LocalDateTime dataHora, TipoHistoricoOcorrencia tipo,
        SeveridadeOcorrencia severidade, StatusOcorrenciaCultivo status, String descricao, String responsavel) {
}
