package com.example.sitiopro.agricultura.repository;

import com.example.sitiopro.agricultura.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {
    Page<Cultivo> findByPropriedadeIdOrderByIdDesc(Long propriedadeId, Pageable pagina);
    Optional<Cultivo> findByIdAndPropriedadeId(Long id, Long propriedadeId);
    boolean existsBySafraIdAndStatusIn(Long safraId, Collection<StatusCultivo> statuses);
    @Query("""
            select (count(c) > 0) from Cultivo c where c.safra.id = :safraId and (
                c.dataPlantio < :inicio or c.dataPlantio > :fim or c.dataColheitaReal > :fim
                or exists (select p.id from Plantio p where p.cultivo = c and (p.data < :inicio or p.data > :fim))
                or exists (select h.id from Colheita h where h.cultivo = c and (h.data < :inicio or h.data > :fim))
                or exists (select a.id from AcompanhamentoCultivo a where a.cultivo = c
                    and (cast(a.dataHora as date) < :inicio or cast(a.dataHora as date) > :fim)))
            """)
    boolean existsForaDoPeriodo(Long safraId, LocalDate inicio, LocalDate fim);
    long countByPropriedadeIdAndStatusIn(Long propriedadeId, Collection<StatusCultivo> statuses);
    List<Cultivo> findByPropriedadeIdAndStatusInAndPrevisaoColheitaLessThanEqualOrderByPrevisaoColheitaAscIdAsc(
            Long propriedadeId, Collection<StatusCultivo> statuses, LocalDate limite, Pageable pagina);
    @Query("select coalesce(sum(c.areaCultivadaHa), 0) from Cultivo c where c.talhao.id = :talhaoId and c.status in :statuses and c.id <> :ignorarId")
    BigDecimal areaReservada(Long talhaoId, Collection<StatusCultivo> statuses, Long ignorarId);
    @Query("select coalesce(sum(c.areaCultivadaHa), 0) from Cultivo c where c.propriedade.id = :propriedadeId and c.status in :statuses")
    BigDecimal areaAtiva(Long propriedadeId, Collection<StatusCultivo> statuses);
    @Query("""
            select t from Tarefa t, Cultivo c
            where c.propriedade.id = :propriedadeId and t.moduloOrigem = :modulo
            and t.referenciaOrigem = concat('CULTIVO:', cast(c.id as string))
            and t.status in :statuses
            order by case when t.dataVencimento is null then 1 else 0 end, t.dataVencimento, t.id
            """)
    List<com.example.sitiopro.tarefas.entity.Tarefa> proximosTrabalhos(Long propriedadeId,
            com.example.sitiopro.tarefas.entity.ModuloOrigem modulo,
            Collection<com.example.sitiopro.tarefas.entity.StatusTarefa> statuses, Pageable pagina);
}
