package com.example.sitiopro.manutencao.repository;

import com.example.sitiopro.manutencao.entity.LeituraMedidorAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface LeituraMedidorAtivoRepository extends JpaRepository<LeituraMedidorAtivo, Long> {
    Optional<LeituraMedidorAtivo> findByChaveIdempotencia(String chave);
    List<LeituraMedidorAtivo> findByAtivoPatrimonialIdOrderByDataLeituraDescIdDesc(Long ativoId);
    Optional<LeituraMedidorAtivo> findTopByAtivoPatrimonialIdAndHorimetroIsNotNullOrderByDataLeituraDescIdDesc(Long ativoId);
    Optional<LeituraMedidorAtivo> findTopByAtivoPatrimonialIdAndQuilometragemIsNotNullOrderByDataLeituraDescIdDesc(Long ativoId);
    Optional<LeituraMedidorAtivo> findTopByAtivoPatrimonialIdOrderByDataLeituraDescIdDesc(Long ativoId);
}
