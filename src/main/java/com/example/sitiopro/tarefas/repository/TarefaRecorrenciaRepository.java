package com.example.sitiopro.tarefas.repository;

import com.example.sitiopro.tarefas.entity.TarefaRecorrencia;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TarefaRecorrenciaRepository extends JpaRepository<TarefaRecorrencia, Long> {

    @EntityGraph(attributePaths = {"tarefaModelo", "tarefaModelo.responsavel", "tarefaModelo.criadoPorUsuario"})
    Optional<TarefaRecorrencia> findByTarefaModeloId(Long tarefaModeloId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"tarefaModelo", "tarefaModelo.responsavel", "tarefaModelo.criadoPorUsuario"})
    @Query("select r from TarefaRecorrencia r where r.tarefaModelo.id = :tarefaModeloId")
    Optional<TarefaRecorrencia> buscarPorModeloParaAtualizacao(@Param("tarefaModeloId") Long tarefaModeloId);

    @Query("""
            select r.id from TarefaRecorrencia r
            where r.ativa = true and r.proximaOcorrenciaEm <= :agora
            order by r.proximaOcorrenciaEm, r.id
            """)
    List<Long> buscarIdsVencidos(@Param("agora") LocalDateTime agora, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"tarefaModelo", "tarefaModelo.responsavel", "tarefaModelo.criadoPorUsuario"})
    @Query("select r from TarefaRecorrencia r where r.id = :id")
    Optional<TarefaRecorrencia> buscarParaAtualizacao(@Param("id") Long id);
}
