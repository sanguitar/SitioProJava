package com.example.sitiopro.integracao.embrapa.agrofit.repository;

import com.example.sitiopro.integracao.embrapa.agrofit.entity.AgrofitCultura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgrofitCulturaRepository extends JpaRepository<AgrofitCultura, Long> {

    List<AgrofitCultura> findAllByOrderByNome();
}
