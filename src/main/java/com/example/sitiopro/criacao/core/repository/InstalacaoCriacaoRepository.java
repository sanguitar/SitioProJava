package com.example.sitiopro.criacao.core.repository;

import com.example.sitiopro.criacao.core.entity.InstalacaoCriacao;
import com.example.sitiopro.criacao.core.entity.TipoInstalacaoCriacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InstalacaoCriacaoRepository extends JpaRepository<InstalacaoCriacao, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    List<InstalacaoCriacao> findByAtivoTrueOrderByNomeAsc();
    List<InstalacaoCriacao> findByAtivoTrueAndTipoOrderByNomeAsc(TipoInstalacaoCriacao tipo);
    Page<InstalacaoCriacao> findAllByOrderByAtivoDescNomeAsc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InstalacaoCriacao i where i.id = :id")
    Optional<InstalacaoCriacao> buscarParaAtualizacao(@Param("id") Long id);
}
