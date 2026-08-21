package com.example.sitiopro.integracao.core.repository;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.entity.IntegracaoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface IntegracaoEstadoRepository extends JpaRepository<IntegracaoEstado, FonteIntegracao> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select estado from IntegracaoEstado estado where estado.fonte = :fonte")
    Optional<IntegracaoEstado> buscarParaAtualizacao(@Param("fonte") FonteIntegracao fonte);
}
