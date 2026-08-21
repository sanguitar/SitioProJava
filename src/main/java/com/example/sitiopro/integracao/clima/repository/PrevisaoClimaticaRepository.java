package com.example.sitiopro.integracao.clima.repository;

import com.example.sitiopro.integracao.clima.entity.PrevisaoClimatica;
import com.example.sitiopro.integracao.core.FonteIntegracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrevisaoClimaticaRepository extends JpaRepository<PrevisaoClimatica, Long> {

    List<PrevisaoClimatica> findByFonteAndContextoAndDataHoraPrevisaoBetweenOrderByDataHoraPrevisao(
            FonteIntegracao fonte, String contexto, LocalDateTime inicio, LocalDateTime fim);

    Optional<PrevisaoClimatica> findFirstByFonteAndContextoAndDataHoraPrevisaoLessThanEqualOrderByDataHoraPrevisaoDesc(
            FonteIntegracao fonte, String contexto, LocalDateTime dataHora);

    Optional<PrevisaoClimatica> findFirstByFonteAndContextoAndDataHoraPrevisaoGreaterThanOrderByDataHoraPrevisao(
            FonteIntegracao fonte, String contexto, LocalDateTime dataHora);

    Optional<PrevisaoClimatica> findFirstByFonteAndContextoOrderByObtidoEmDesc(
            FonteIntegracao fonte, String contexto);

    long deleteByFonteAndContextoAndDataHoraPrevisaoBefore(
            FonteIntegracao fonte, String contexto, LocalDateTime limite);
}
