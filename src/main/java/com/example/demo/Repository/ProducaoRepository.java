package com.example.demo.Repository;

import com.example.demo.Model.Producao;
import com.example.demo.Model.Categoria;
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

    // ADICIONE ESTA LINHA EXATA AQUI:
    long countByCategoria(Categoria categoria);
}