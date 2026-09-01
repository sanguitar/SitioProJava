package com.example.sitiopro.estoque.repository;

import com.example.sitiopro.estoque.entity.MovimentoEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Collection;
import java.util.Optional;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    @Query(value = """
            SELECT
                m.item_id AS itemId,
                SUM(CASE
                    WHEN m.tipo IN ('ENTRADA', 'AJUSTE_ENTRADA') THEN m.quantidade
                    WHEN m.tipo IN ('CONSUMO', 'PERDA', 'AJUSTE_SAIDA', 'DESCARTE') THEN -m.quantidade
                    ELSE 0
                END) AS saldo,
                SUM(CASE WHEN m.tipo = 'ENTRADA' AND m.custo_total IS NOT NULL
                    THEN m.quantidade ELSE 0 END) AS quantidadeEntradasComCusto,
                SUM(CASE WHEN m.tipo = 'ENTRADA' AND m.custo_total IS NOT NULL
                    THEN m.custo_total ELSE 0 END) AS custoTotalEntradas,
                (SELECT TOP 1 recente.custo_unitario
                 FROM dbo.estoque_movimentos recente
                 WHERE recente.item_id = m.item_id
                   AND recente.tipo = 'ENTRADA'
                   AND recente.custo_unitario IS NOT NULL
                 ORDER BY recente.data_movimento DESC, recente.id DESC) AS ultimoPreco
            FROM dbo.estoque_movimentos m
            GROUP BY m.item_id
            """, nativeQuery = true)
    List<ItemMovimentoAgregado> agregarPorItem();

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    List<MovimentoEstoque> findTop10ByOrderByDataMovimentoDescIdDesc();

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    Page<MovimentoEstoque> findAllByOrderByDataMovimentoDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    List<MovimentoEstoque> findByItemIdOrderByDataMovimentoDescIdDesc(Long itemId);

    List<MovimentoEstoque> findByItemId(Long itemId);

    List<MovimentoEstoque> findByItemIdAndLoteId(Long itemId, Long loteId);

    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    List<MovimentoEstoque> findByLoteIdInOrderByDataMovimentoDescIdDesc(Collection<Long> loteIds);

    @Override
    @EntityGraph(attributePaths = {"item", "item.unidadeMedida", "localOrigem", "localDestino", "lote"})
    Optional<MovimentoEstoque> findById(Long id);

    interface ItemMovimentoAgregado {
        Long getItemId();
        java.math.BigDecimal getSaldo();
        java.math.BigDecimal getQuantidadeEntradasComCusto();
        java.math.BigDecimal getCustoTotalEntradas();
        java.math.BigDecimal getUltimoPreco();
    }
}
