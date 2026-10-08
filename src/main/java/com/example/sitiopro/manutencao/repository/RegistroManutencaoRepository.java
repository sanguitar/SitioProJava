package com.example.sitiopro.manutencao.repository;

import com.example.sitiopro.manutencao.entity.RegistroManutencao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public interface RegistroManutencaoRepository extends JpaRepository<RegistroManutencao, Long> {
    Optional<RegistroManutencao> findByChaveIdempotencia(String chave);
    List<RegistroManutencao> findByAtivoIdOrderByDataManutencaoDescIdDesc(Long ativoId);
    List<RegistroManutencao> findTop10ByOrderByDataManutencaoDescIdDesc();
    List<RegistroManutencao> findByProximaManutencaoConcluidaFalseAndProximaManutencaoBefore(LocalDateTime data);
    long countByProximaManutencaoConcluidaFalseAndProximaManutencaoBefore(LocalDateTime data);
    long countByProximaManutencaoConcluidaFalseAndProximaManutencaoBetween(LocalDateTime inicio, LocalDateTime fim);
    @Query("select coalesce(sum(r.custo), 0) from RegistroManutencao r where r.dataManutencao >= :inicio")
    BigDecimal somarCustosDesde(@Param("inicio") LocalDateTime inicio);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RegistroManutencao r where r.id=:id")
    Optional<RegistroManutencao> buscarParaAtualizacao(@Param("id") Long id);
}
