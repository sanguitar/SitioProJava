package com.example.sitiopro.administracao.configuracao.repository;

import com.example.sitiopro.administracao.configuracao.entity.ConfiguracaoOperacional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConfiguracaoOperacionalRepository extends JpaRepository<ConfiguracaoOperacional, Integer> {
    @Query(value = "SELECT * FROM configuracoes_operacionais WITH (UPDLOCK, HOLDLOCK) WHERE id = 1", nativeQuery = true)
    Optional<ConfiguracaoOperacional> buscarParaInicializar();
}
