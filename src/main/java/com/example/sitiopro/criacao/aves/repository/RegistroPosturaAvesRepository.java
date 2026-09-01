package com.example.sitiopro.criacao.aves.repository;

import com.example.sitiopro.criacao.aves.entity.RegistroPosturaAves;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroPosturaAvesRepository extends JpaRepository<RegistroPosturaAves, Long> {
    interface PosturaDiaria {
        LocalDate getData();
        Long getInteiros();
        Long getQuebrados();
        Long getDescartados();
    }

    Optional<RegistroPosturaAves> findByChaveIdempotencia(String chave);
    @Override
    @EntityGraph(attributePaths = "lote")
    Optional<RegistroPosturaAves> findById(Long id);
    @EntityGraph(attributePaths = "lote")
    List<RegistroPosturaAves> findTop50ByOrderByDataColetaDescIdDesc();
    List<RegistroPosturaAves> findByLoteIdOrderByDataColetaDescIdDesc(Long loteId);
    @Query("select coalesce(sum(p.ovosInteiros), 0) from RegistroPosturaAves p where p.lote.id = :loteId and p.dataColeta between :inicio and :fim")
    Long somarInteiros(@Param("loteId") Long loteId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
    @Query("select coalesce(sum(p.ovosInteiros), 0) from RegistroPosturaAves p where p.dataColeta = :data")
    Long somarInteirosNaData(@Param("data") LocalDate data);

    @Query(value = """
            SELECT p.data_coleta AS data,
                   SUM(p.ovos_inteiros) AS inteiros,
                   SUM(p.ovos_quebrados) AS quebrados,
                   SUM(p.ovos_descartados) AS descartados
            FROM dbo.aves_posturas p
            WHERE p.data_coleta BETWEEN :inicio AND :fim
            GROUP BY p.data_coleta
            ORDER BY p.data_coleta
            """, nativeQuery = true)
    List<PosturaDiaria> agregarPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
