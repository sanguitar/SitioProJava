package com.example.sitiopro.compras.repository;

import com.example.sitiopro.compras.entity.Compra;
import com.example.sitiopro.compras.entity.StatusCompra;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    @EntityGraph(attributePaths = {"fornecedor"})
    @Query("""
            select c from Compra c
            where (:status is null or c.status = :status)
              and (:fornecedorId is null or c.fornecedor.id = :fornecedorId)
              and (:inicio is null or c.dataCompra >= :inicio)
              and (:fim is null or c.dataCompra <= :fim)
            order by c.dataCompra desc, c.id desc
            """)
    List<Compra> buscar(@Param("status") StatusCompra status,
            @Param("fornecedorId") Long fornecedorId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @EntityGraph(attributePaths = {"fornecedor"})
    List<Compra> findTop10ByOrderByDataCompraDescIdDesc();

    @EntityGraph(attributePaths = {"fornecedor"})
    Optional<Compra> findFirstByStatusOrderByDataCompraDescIdDesc(StatusCompra status);

    long countByStatus(StatusCompra status);

    @Query("""
            select coalesce(sum(c.total), 0)
            from Compra c
            where c.status = com.example.sitiopro.compras.entity.StatusCompra.CONFIRMADA
              and c.dataCompra between :inicio and :fim
            """)
    BigDecimal somarTotalConfirmadoEntre(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select count(c)
            from Compra c
            where c.status = com.example.sitiopro.compras.entity.StatusCompra.CONFIRMADA
              and c.dataCompra between :inicio and :fim
            """)
    long contarConfirmadasEntre(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Override
    @EntityGraph(attributePaths = {
            "fornecedor",
            "itens",
            "itens.itemEstoque",
            "itens.itemEstoque.unidadeMedida",
            "itens.localDestino",
            "itens.movimentoEstoque"
    })
    Optional<Compra> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {
            "fornecedor",
            "itens",
            "itens.itemEstoque",
            "itens.itemEstoque.unidadeMedida",
            "itens.localDestino",
            "itens.movimentoEstoque"
    })
    @Query("select c from Compra c where c.id = :id")
    Optional<Compra> buscarParaConfirmacao(@Param("id") Long id);
}
