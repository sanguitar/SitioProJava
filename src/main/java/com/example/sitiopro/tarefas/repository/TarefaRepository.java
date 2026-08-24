package com.example.sitiopro.tarefas.repository;

import com.example.sitiopro.tarefas.entity.PrioridadeTarefa;
import com.example.sitiopro.tarefas.entity.StatusTarefa;
import com.example.sitiopro.tarefas.entity.Tarefa;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @EntityGraph(attributePaths = {"responsavel", "criadoPorUsuario"})
    @Query("""
            select t from Tarefa t
            where (:status is null or t.status = :status)
              and (:prioridade is null or t.prioridade = :prioridade)
              and (:responsavelId is null or t.responsavel.id = :responsavelId)
              and (:somenteVencidas = false or (
                    t.dataVencimento < :agora
                    and t.status in (com.example.sitiopro.tarefas.entity.StatusTarefa.PENDENTE,
                                     com.example.sitiopro.tarefas.entity.StatusTarefa.EM_ANDAMENTO)))
              and (:inicioPrazo is null or t.dataVencimento >= :inicioPrazo)
              and (:fimPrazo is null or t.dataVencimento < :fimPrazo)
            order by
              case t.prioridade
                when com.example.sitiopro.tarefas.entity.PrioridadeTarefa.CRITICA then 0
                when com.example.sitiopro.tarefas.entity.PrioridadeTarefa.ALTA then 1
                when com.example.sitiopro.tarefas.entity.PrioridadeTarefa.NORMAL then 2
                else 3
              end,
              case when t.dataVencimento is null then 1 else 0 end,
              t.dataVencimento,
              t.id desc
            """)
    Page<Tarefa> buscar(@Param("status") StatusTarefa status,
            @Param("prioridade") PrioridadeTarefa prioridade,
            @Param("responsavelId") Long responsavelId,
            @Param("somenteVencidas") boolean somenteVencidas,
            @Param("agora") LocalDateTime agora,
            @Param("inicioPrazo") LocalDateTime inicioPrazo,
            @Param("fimPrazo") LocalDateTime fimPrazo,
            Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"responsavel", "criadoPorUsuario", "recorrenciaOrigem"})
    Optional<Tarefa> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"responsavel", "criadoPorUsuario", "recorrenciaOrigem"})
    @Query("select t from Tarefa t where t.id = :id")
    Optional<Tarefa> buscarParaAtualizacao(@Param("id") Long id);

    long countByStatusInAndDataVencimentoGreaterThanEqualAndDataVencimentoLessThan(
            Collection<StatusTarefa> statuses, LocalDateTime inicio, LocalDateTime fim);

    long countByStatusInAndDataVencimentoBefore(Collection<StatusTarefa> statuses, LocalDateTime agora);

    long countByStatusInAndPrioridade(Collection<StatusTarefa> statuses, PrioridadeTarefa prioridade);

    boolean existsByRecorrenciaOrigemIdAndOcorrenciaProgramadaEm(Long recorrenciaId,
            LocalDateTime ocorrenciaProgramadaEm);
}
