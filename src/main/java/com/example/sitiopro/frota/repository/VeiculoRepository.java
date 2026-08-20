package com.example.sitiopro.frota.repository;

import com.example.sitiopro.frota.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    List<Veiculo> findBySituacao(String situacao);

    List<Veiculo> findByTipo(String tipo);
}
