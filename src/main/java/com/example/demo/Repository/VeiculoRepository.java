package com.example.demo.Repository;

import com.example.demo.Model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    // Busca veículos pela situação (ex: todos em "Manutenção")
    List<Veiculo> findBySituacao(String situacao);

    // Busca por tipo (ex: todos os "Tratores")
    List<Veiculo> findByTipo(String tipo);
}