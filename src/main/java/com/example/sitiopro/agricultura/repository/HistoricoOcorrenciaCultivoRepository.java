package com.example.sitiopro.agricultura.repository;

import com.example.sitiopro.agricultura.entity.HistoricoOcorrenciaCultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HistoricoOcorrenciaCultivoRepository extends JpaRepository<HistoricoOcorrenciaCultivo, Long> {
    List<HistoricoOcorrenciaCultivo> findByOcorrenciaIdOrderByDataHoraDescIdDesc(Long ocorrenciaId);
    Optional<HistoricoOcorrenciaCultivo> findByOcorrenciaIdAndChaveIdempotencia(
            Long ocorrenciaId, String chaveIdempotencia);
}
