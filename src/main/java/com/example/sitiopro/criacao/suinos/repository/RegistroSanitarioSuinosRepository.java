package com.example.sitiopro.criacao.suinos.repository;

import com.example.sitiopro.criacao.suinos.entity.RegistroSanitarioSuinos;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.*;

public interface RegistroSanitarioSuinosRepository extends JpaRepository<RegistroSanitarioSuinos, Long> {
    @EntityGraph(attributePaths = {"lote", "animalReprodutivo", "itemEstoque", "localEstoque", "movimentoEstoque"})
    Optional<RegistroSanitarioSuinos> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths = {"lote", "animalReprodutivo", "itemEstoque", "localEstoque", "movimentoEstoque"})
    List<RegistroSanitarioSuinos> findAllByOrderByDataProcedimentoDescIdDesc();
    @EntityGraph(attributePaths = {"lote", "animalReprodutivo", "itemEstoque", "localEstoque", "movimentoEstoque"})
    List<RegistroSanitarioSuinos> findByLoteIdOrderByDataProcedimentoDescIdDesc(Long loteId);
    @EntityGraph(attributePaths = {"lote", "animalReprodutivo", "itemEstoque", "localEstoque", "movimentoEstoque"})
    List<RegistroSanitarioSuinos> findByAnimalReprodutivoIdOrderByDataProcedimentoDescIdDesc(Long animalId);
    @EntityGraph(attributePaths = {"lote", "animalReprodutivo", "itemEstoque", "localEstoque", "movimentoEstoque"})
    @Override Optional<RegistroSanitarioSuinos> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"lote", "animalReprodutivo", "itemEstoque", "localEstoque", "movimentoEstoque"})
    @Query("select r from RegistroSanitarioSuinos r where r.id=:id")
    Optional<RegistroSanitarioSuinos> buscarParaAtualizacao(@Param("id") Long id);
    @EntityGraph(attributePaths = {"lote", "animalReprodutivo"})
    List<RegistroSanitarioSuinos> findByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(LocalDate data);
    long countByDataProcedimentoGreaterThanEqual(LocalDate data);
    long countByProximaAcaoConcluidaFalseAndProximaAcaoDataGreaterThanEqual(LocalDate data);
    long countByProximaAcaoConcluidaFalseAndProximaAcaoDataBefore(LocalDate data);
}
