package com.example.sitiopro.agricultura.repository;

import com.example.sitiopro.agricultura.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;

public interface SafraRepository extends JpaRepository<Safra, Long> {
    Page<Safra> findByPropriedadeIdOrderByDataInicioDescIdDesc(Long propriedadeId, Pageable pagina);
    List<Safra> findByPropriedadeIdOrderByDataInicioDescIdDesc(Long propriedadeId);
    Optional<Safra> findByIdAndPropriedadeId(Long id, Long propriedadeId);
    Optional<Safra> findFirstByPropriedadeIdAndStatusOrderByDataInicioDescIdDesc(Long propriedadeId, StatusSafra status);
    boolean existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(Long propriedadeId, String nome, Long id);
}
