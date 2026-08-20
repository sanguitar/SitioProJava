package com.example.sitiopro.producao.service;

import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProducaoService {

    private static final List<String> STATUS_ALERTA = List.of("Estoque Baixo", "Necessário comprar");

    private final ProducaoRepository producaoRepository;

    public ProducaoService(ProducaoRepository producaoRepository) {
        this.producaoRepository = producaoRepository;
    }

    public Page<Producao> listarPaginado(Long categoriaId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        if (categoriaId != null) {
            return producaoRepository.findByCategoriaId(categoriaId, pageable);
        }
        return producaoRepository.findAll(pageable);
    }

    public List<Producao> listarTodos() {
        return producaoRepository.findAll();
    }

    public long contarTodos() {
        return producaoRepository.count();
    }

    public long contarPorCategoria(Categoria categoria) {
        return producaoRepository.countByCategoria(categoria);
    }

    public long contarAlertas() {
        return producaoRepository.findAll().stream()
                .filter(producao -> producao.getStatus() != null)
                .filter(producao -> STATUS_ALERTA.stream()
                        .anyMatch(status -> status.equalsIgnoreCase(producao.getStatus())))
                .count();
    }

    public Producao novo() {
        return new Producao();
    }

    public Producao buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return producaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));
    }

    public Producao salvar(Producao producao) {
        if (producao == null) {
            throw new IllegalArgumentException("Produção não pode ser nula");
        }
        return producaoRepository.save(producao);
    }

    public void excluir(Long id) {
        if (id != null) {
            producaoRepository.deleteById(id);
        }
    }
}
