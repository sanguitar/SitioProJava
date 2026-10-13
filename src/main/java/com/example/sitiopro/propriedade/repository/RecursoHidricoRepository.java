package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.entity.RecursoHidrico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface RecursoHidricoRepository extends JpaRepository<RecursoHidrico, Long> {
    Page<RecursoHidrico> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId, Pageable pageable);
    Optional<RecursoHidrico> findByIdAndPropriedadeId(Long id, Long propriedadeId);
    long countByPropriedadeId(Long propriedadeId);
    boolean existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(Long propriedadeId, String nome, Long id);
    List<RecursoHidrico> findByPropriedadeIdOrderByNomeAsc(Long propriedadeId);
    List<RecursoHidrico> findByPropriedadeIdAndAtivoTrueOrderByNomeAsc(Long propriedadeId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecursoHidrico r where r.id = :id and r.propriedade.id = :propriedadeId")
    Optional<RecursoHidrico> buscarParaAtualizacao(@Param("id") Long id,
            @Param("propriedadeId") Long propriedadeId);
}
