package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.AlimentacaoAves;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AlimentacaoAvesRepository extends JpaRepository<AlimentacaoAves, Long> {
    interface ConsumoDiario {
        LocalDate getData();
        String getUnidade();
        BigDecimal getQuantidade();
    }

    Optional<AlimentacaoAves> findByChaveIdempotencia(String chave);
    @EntityGraph(attributePaths = {"itemEstoque", "itemEstoque.unidadeMedida", "localEstoque", "movimentoEstoque"})
    List<AlimentacaoAves> findByLoteIdOrderByDataEventoDescIdDesc(Long loteId);

    @Query(value = """
            SELECT CAST(a.data_evento AS DATE) AS data,
                   u.sigla AS unidade,
                   SUM(a.quantidade) AS quantidade
            FROM dbo.aves_alimentacoes a
            INNER JOIN dbo.estoque_itens i ON i.id = a.item_estoque_id
            INNER JOIN dbo.estoque_unidades_medida u ON u.id = i.unidade_medida_id
            WHERE a.data_evento >= :inicio AND a.data_evento < :fimExclusivo
            GROUP BY CAST(a.data_evento AS DATE), u.sigla
            ORDER BY unidade, data
            """, nativeQuery = true)
    List<ConsumoDiario> agregarPorDiaEUnidade(@Param("inicio") LocalDate inicio,
            @Param("fimExclusivo") LocalDate fimExclusivo);
}
