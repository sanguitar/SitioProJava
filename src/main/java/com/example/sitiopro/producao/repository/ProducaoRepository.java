package com.example.sitiopro.producao.repository;

import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.producao.model.Producao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface ProducaoRepository extends JpaRepository<Producao, Long> {

    @Override
    @NonNull
    Page<Producao> findAll(@NonNull Pageable pageable);

    Page<Producao> findByCategoriaId(Long id, Pageable pageable);

    long countByCategoria(Categoria categoria);
}
