package com.example.sitiopro.producao.service;

import com.example.sitiopro.categoria.model.Categoria;
import com.example.sitiopro.estoque.entity.CategoriaEstoque;
import com.example.sitiopro.estoque.service.EstoqueCatalogoService;
import com.example.sitiopro.producao.dto.ProducaoForm;
import com.example.sitiopro.producao.model.Producao;
import com.example.sitiopro.producao.repository.ProducaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProducaoService {

    private static final List<String> STATUS_ALERTA = List.of("Estoque Baixo", "Necessário comprar");

    private final ProducaoRepository producaoRepository;
    private final EstoqueCatalogoService estoqueCatalogoService;

    public ProducaoService(ProducaoRepository producaoRepository, EstoqueCatalogoService estoqueCatalogoService) {
        this.producaoRepository = producaoRepository;
        this.estoqueCatalogoService = estoqueCatalogoService;
    }

    @Transactional(readOnly = true)
    public Page<Producao> listarPaginado(Long categoriaId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size);
        if (categoriaId != null) {
            return producaoRepository.findByCategoriaEstoqueId(categoriaId, pageable);
        }
        return producaoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Producao> listarTodos() {
        return producaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long contarTodos() {
        return producaoRepository.count();
    }

    @Transactional(readOnly = true)
    public long contarPorCategoria(Categoria categoria) {
        return producaoRepository.countByCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public long contarAlertas() {
        return producaoRepository.findAll().stream()
                .filter(producao -> producao.getStatus() != null)
                .filter(producao -> STATUS_ALERTA.stream()
                        .anyMatch(status -> status.equalsIgnoreCase(producao.getStatus())))
                .count();
    }

    public ProducaoForm novoFormulario() {
        ProducaoForm form = new ProducaoForm();
        form.setStatus("Estoque");
        return form;
    }

    @Transactional(readOnly = true)
    public Producao buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        return producaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public ProducaoForm formularioEdicao(Long id) {
        Producao producao = buscarPorId(id);
        ProducaoForm form = new ProducaoForm();
        form.setId(producao.getId());
        if (producao.getCategoriaEstoque() != null) {
            form.setCategoriaId(producao.getCategoriaEstoque().getId());
        }
        form.setItem(producao.getItem());
        form.setQuantidade(producao.getQuantidade());
        form.setUnidade(producao.getUnidade());
        form.setStatus(producao.getStatus());
        return form;
    }

    @Transactional
    public Producao salvar(ProducaoForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Cadastro rural não pode ser nulo");
        }
        CategoriaEstoque categoria = estoqueCatalogoService.buscarCategoriaAtiva(form.getCategoriaId());
        Producao producao = form.getId() == null ? new Producao() : buscarPorId(form.getId());
        producao.setCategoriaEstoque(categoria);
        producao.setItem(normalizar(form.getItem()));
        producao.setQuantidade(form.getQuantidade());
        producao.setUnidade(normalizar(form.getUnidade()));
        producao.setStatus(normalizar(form.getStatus()));
        return producaoRepository.save(producao);
    }

    @Transactional
    public void excluir(Long id) {
        if (id != null) {
            producaoRepository.deleteById(id);
        }
    }

    private String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }
}
