package com.example.sitiopro.criacao.suinos.repository;

import com.example.sitiopro.criacao.suinos.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface AnimalReprodutivoSuinosRepository extends JpaRepository<AnimalReprodutivoSuinos, Long> {
    Optional<AnimalReprodutivoSuinos> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths = "lote")
    List<AnimalReprodutivoSuinos> findByStatusOrderByTipoAscCodigoAsc(StatusAnimalReprodutivo status);
    @EntityGraph(attributePaths = "lote")
    List<AnimalReprodutivoSuinos> findByTipoAndStatusOrderByCodigoAsc(
            TipoAnimalReprodutivo tipo, StatusAnimalReprodutivo status);
    long countByTipoAndStatus(TipoAnimalReprodutivo tipo, StatusAnimalReprodutivo status);
    long countByLoteIdAndStatus(Long loteId, StatusAnimalReprodutivo status);
    @EntityGraph(attributePaths = "lote") @Override Optional<AnimalReprodutivoSuinos> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @EntityGraph(attributePaths = "lote")
    @Query("select a from AnimalReprodutivoSuinos a where a.id=:id")
    Optional<AnimalReprodutivoSuinos> buscarParaAtualizacao(@Param("id") Long id);
}
