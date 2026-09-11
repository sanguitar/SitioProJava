package com.example.sitiopro.agricultura.repository;

import com.example.sitiopro.agricultura.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;

public interface AcompanhamentoCultivoRepository extends JpaRepository<AcompanhamentoCultivo, Long> {
    List<AcompanhamentoCultivo> findByCultivoIdOrderByDataHoraDescIdDesc(Long cultivoId);
}
