package com.example.sitiopro.tarefas.repository;

import com.example.sitiopro.tarefas.entity.Alerta;
import com.example.sitiopro.tarefas.entity.ModuloOrigem;
import com.example.sitiopro.tarefas.entity.SeveridadeAlerta;
import com.example.sitiopro.tarefas.entity.StatusAlerta;
import com.example.sitiopro.tarefas.entity.TipoAlerta;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    interface PainelContadores {
        Long getAtivos();

        Long getCriticos();

        Long getAltaSeveridade();
    }

    @EntityGraph(attributePaths = {"reconhecidoPor", "tarefa"})
    @Query("""
            select a from Alerta a
            where (:status is null or a.status = :status)
              and (:severidade is null or a.severidade = :severidade)
              and (:modulo is null or a.moduloOrigem = :modulo)
              and (:somenteAtivos = false or a.status <> com.example.sitiopro.tarefas.entity.StatusAlerta.RESOLVIDO)
            order by
              case a.severidade
                when com.example.sitiopro.tarefas.entity.SeveridadeAlerta.CRITICA then 0
                when com.example.sitiopro.tarefas.entity.SeveridadeAlerta.ALTA then 1
                when com.example.sitiopro.tarefas.entity.SeveridadeAlerta.ATENCAO then 2
                else 3
              end,
              a.detectadoEm desc,
              a.id desc
            """)
    Page<Alerta> buscar(@Param("status") StatusAlerta status,
            @Param("severidade") SeveridadeAlerta severidade,
            @Param("modulo") ModuloOrigem modulo,
            @Param("somenteAtivos") boolean somenteAtivos,
            Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"reconhecidoPor", "tarefa"})
    Optional<Alerta> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"reconhecidoPor", "tarefa"})
    @Query("select a from Alerta a where a.id = :id")
    Optional<Alerta> buscarParaAtualizacao(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select a from Alerta a
            where a.moduloOrigem = :modulo
              and a.tipo = :tipo
              and a.status in :statuses
            order by a.id
            """)
    List<Alerta> buscarAbertosParaAtualizacao(@Param("modulo") ModuloOrigem modulo,
            @Param("tipo") TipoAlerta tipo,
            @Param("statuses") Collection<StatusAlerta> statuses);

    long countByStatusIn(Collection<StatusAlerta> statuses);

    long countByStatusInAndSeveridade(Collection<StatusAlerta> statuses, SeveridadeAlerta severidade);

    @Query("""
            select
              coalesce(sum(case when a.status in :abertos then 1 else 0 end), 0) as ativos,
              coalesce(sum(case when a.status in :abertos
                and a.severidade = com.example.sitiopro.tarefas.entity.SeveridadeAlerta.CRITICA then 1 else 0 end), 0)
                as criticos,
              coalesce(sum(case when a.status in :abertos
                and a.severidade = com.example.sitiopro.tarefas.entity.SeveridadeAlerta.ALTA then 1 else 0 end), 0)
                as altaSeveridade
            from Alerta a
            """)
    PainelContadores contarParaPainel(@Param("abertos") Collection<StatusAlerta> abertos);

    @EntityGraph(attributePaths = {"reconhecidoPor", "tarefa"})
    @Query("""
            select a from Alerta a
            where a.status in :abertos
            order by
              case a.severidade
                when com.example.sitiopro.tarefas.entity.SeveridadeAlerta.CRITICA then 0
                when com.example.sitiopro.tarefas.entity.SeveridadeAlerta.ALTA then 1
                when com.example.sitiopro.tarefas.entity.SeveridadeAlerta.ATENCAO then 2
                else 3
              end,
              a.detectadoEm desc,
              a.id desc
            """)
    List<Alerta> buscarDestaquesPainel(@Param("abertos") Collection<StatusAlerta> abertos,
            Pageable pageable);

    @EntityGraph(attributePaths = {"reconhecidoPor", "tarefa"})
    List<Alerta> findByModuloOrigemAndReferenciaOrigemAndStatusInOrderByDetectadoEmDesc(
            ModuloOrigem modulo, String referencia, Collection<StatusAlerta> statuses);

    long countByModuloOrigemAndStatusIn(ModuloOrigem modulo, Collection<StatusAlerta> statuses);
}
