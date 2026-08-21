package com.example.sitiopro.integracao.core.repository;

import com.example.sitiopro.integracao.core.FonteIntegracao;
import com.example.sitiopro.integracao.core.entity.IntegracaoExecucao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IntegracaoExecucaoRepository extends JpaRepository<IntegracaoExecucao, Long> {

    Optional<IntegracaoExecucao> findFirstByFonteOrderByIniciadoEmDescIdDesc(FonteIntegracao fonte);

    List<IntegracaoExecucao> findByFonteOrderByIniciadoEmDescIdDesc(FonteIntegracao fonte, Pageable pageable);
}
