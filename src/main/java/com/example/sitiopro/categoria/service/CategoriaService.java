package com.example.sitiopro.categoria.service;

import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.categoria.repository.CategoriaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria nova() {
        Categoria categoria = new Categoria();
        categoria.setCorHex("#1e3d1a");
        return categoria;
    }

    public Categoria salvar(Categoria categoria) {
        if (categoria == null || !StringUtils.hasText(categoria.getNome())) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório");
        }
        if (!StringUtils.hasText(categoria.getIcone())) {
            categoria.setIcone("fa-folder");
        }
        if (!StringUtils.hasText(categoria.getCorHex())) {
            categoria.setCorHex("#1e3d1a");
        }
        return categoriaRepository.save(categoria);
    }

    public void excluir(Long id) {
        if (id == null) {
            return;
        }
        try {
            categoriaRepository.deleteById(id);
        } catch (DataIntegrityViolationException ignored) {
            // Mantém o comportamento anterior: categorias vinculadas a itens não são removidas.
        }
    }
}
