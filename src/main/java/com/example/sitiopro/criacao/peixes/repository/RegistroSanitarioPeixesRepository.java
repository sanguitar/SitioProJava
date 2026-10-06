package com.example.sitiopro.criacao.peixes.repository;

import com.example.sitiopro.criacao.peixes.entity.RegistroSanitarioPeixes;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;

public interface RegistroSanitarioPeixesRepository extends JpaRepository<RegistroSanitarioPeixes, Long> {
    @EntityGraph(attributePaths = {"lote", "itemEstoque", "localEstoque", "movimentoEstoque"})
    Optional<RegistroSanitarioPeixes> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths = {"lote", "itemEstoque", "localEstoque", "movimentoEstoque"})
    List<RegistroSanitarioPeixes> findAllByOrderByDataProcedimentoDescIdDesc();
    @EntityGraph(attributePaths = {"lote", "itemEstoque", "localEstoque", "movimentoEstoque"})
    List<RegistroSanitarioPeixes> findByLoteIdOrderByDataProcedimentoDescIdDesc(Long loteId);
    @EntityGraph(attributePaths = {"lote", "itemEstoque", "localEstoque", "movimentoEstoque"})
    @Override Optional<RegistroSanitarioPeixes> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"lote", "itemEstoque", "localEstoque", "movimentoEstoque"})
    @Query("select r from RegistroSanitarioPeixes r where r.id=:id")
    Optional<RegistroSanitarioPeixes> buscarParaAtualizacao(@Param("id") Long id);
    @EntityGraph(attributePaths = "lote")
    List<RegistroSanitarioPeixes> findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(LocalDateTime data);
    long countByDataProcedimentoGreaterThanEqual(LocalDateTime data);
    long countByProximaAcaoConcluidaFalseAndProximaAcaoDataGreaterThanEqual(LocalDateTime data);
    long countByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(LocalDateTime data);
}
