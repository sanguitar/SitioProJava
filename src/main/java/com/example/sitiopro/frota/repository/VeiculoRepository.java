package com.example.sitiopro.frota.repository;

import com.example.sitiopro.frota.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    List<Veiculo> findBySituacao(String situacao);

    List<Veiculo> findByTipo(String tipo);

    List<Veiculo> findAllByOrderByNomeAsc();

    Optional<Veiculo> findByChaveIdempotencia(String chaveIdempotencia);

    boolean existsByAtivoPatrimonialIdAndIdNot(Long ativoPatrimonialId, Long id);

    boolean existsByAtivoPatrimonialId(Long ativoPatrimonialId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Veiculo v where v.id = :id")
    Optional<Veiculo> buscarParaAtualizacao(@Param("id") Long id);
}
