package com.example.demo.Controller;

import com.example.demo.Model.Producao;
import com.example.demo.Model.Categoria;
import com.example.demo.Repository.ProducaoRepository;
import com.example.demo.Repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class SitioController {

        @Autowired
        private ProducaoRepository producaoRepository;

        @Autowired
        private CategoriaRepository categoriaRepository;

        // 1. PAINEL PRINCIPAL (VERSÃO ÚNICA E PAGINADA)
        @GetMapping("/sitio/painel")
        public String exibirPainel(
                        @RequestParam(value = "categoriaId", required = false) Long categoriaId,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {

                Pageable pageable = PageRequest.of(page, 10);
                Page<Producao> paginaEstoque;

                // BUSCA PAGINADA
                if (categoriaId != null) {
                        paginaEstoque = producaoRepository.findByCategoriaId(categoriaId, pageable);
                } else {
                        paginaEstoque = producaoRepository.findAll(pageable);
                }

                // --- NOVA LÓGICA PARA O GRÁFICO (SÍTIO TODO) ---
                List<Categoria> todasCategorias = categoriaRepository.findAll();

                // Criamos uma lista de contagens. O cast (Long) ajuda o compilador a não se
                // perder
                List<Long> contagensGerais = todasCategorias.stream()
                                .map(cat -> (Long) producaoRepository.countByCategoria(cat))
                                .toList();

                List<String> nomesCategorias = todasCategorias.stream()
                                .map(Categoria::getNome)
                                .toList();

                model.addAttribute("labelsGrafico", nomesCategorias);
                model.addAttribute("dadosGrafico", contagensGerais);
                // -----------------------------------------------

                // Atributos para a View
                model.addAttribute("itens", paginaEstoque.getContent());
                model.addAttribute("paginaAtual", page);
                model.addAttribute("totalPaginas", paginaEstoque.getTotalPages());
                model.addAttribute("totalItens", producaoRepository.count());
                model.addAttribute("totalCategorias", todasCategorias.size());
                model.addAttribute("categoriaSelecionada", categoriaId);
                model.addAttribute("categorias", todasCategorias);
                model.addAttribute("usuario", "Systems Analyst");

                // Cálculo de alertas (para os cards do topo)
                long itensAlerta = producaoRepository.findAll().stream()
                                .filter(p -> "Estoque Baixo".equalsIgnoreCase(p.getStatus())
                                                || "Necessário comprar".equalsIgnoreCase(p.getStatus()))
                                .count();
                model.addAttribute("itensAlerta", itensAlerta);

                return "painel";
        }

        // 2. FORMULÁRIO DE CADASTRO
        @GetMapping("/sitio/cadastro")
        public String mostrarFormulario(Model model) {
                model.addAttribute("categorias", categoriaRepository.findAll());
                model.addAttribute("producao", new Producao());
                return "cadastro";
        }

        // 3. EDITAR
        @GetMapping("/sitio/editar/{id}")
        public String mostrarEditar(@PathVariable("id") Long id, Model model) {
                if (id == null) {
                        throw new IllegalArgumentException("ID não pode ser nulo");
                }
                Producao item = producaoRepository.findById(id)
                                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado: " + id));

                model.addAttribute("producao", item);
                model.addAttribute("categorias", categoriaRepository.findAll());
                return "cadastro";
        }

        // 4. SALVAR
        @PostMapping("/sitio/salvar")
        public String salvarItem(@ModelAttribute Producao producao) {
                if (producao != null) {
                        producaoRepository.save(producao);
                }
                return "redirect:/sitio/painel";
        }

        // 5. EXCLUIR
        @GetMapping("/sitio/excluir/{id}")
        public String excluirItem(@PathVariable("id") Long id) {
                if (id != null) {
                        producaoRepository.deleteById(id);
                }
                return "redirect:/sitio/painel";
        }

        // 6. EXIBIR CONFIGURAÇÕES
        @GetMapping("/sitio/configuracoes")
        public String exibirConfiguracoes(Model model) {
                model.addAttribute("categorias", categoriaRepository.findAll());
                model.addAttribute("novaCategoria", new Categoria()); // Objeto vazio para o formulário
                model.addAttribute("usuario", "Systems Analyst");
                return "configuracoes";
        }

        // 7. SALVAR NOVA CATEGORIA
        @PostMapping("/sitio/configuracoes/categoria/salvar")
        public String salvarCategoria(@ModelAttribute Categoria categoria) {
                if (categoria != null && !categoria.getNome().isEmpty()) {
                        categoriaRepository.save(categoria);
                }
                return "redirect:/sitio/configuracoes";
        }

        // 8. EXCLUIR CATEGORIA (Cuidado: só se não houver produtos nela)
        @GetMapping("/sitio/configuracoes/categoria/excluir/{id}")
        public String excluirCategoria(@PathVariable("id") Long id) {
                if (id != null) {
                        try {
                                categoriaRepository.deleteById(id);
                        } catch (Exception e) {
                                // Se houver itens usando a categoria, o banco vai barrar (Integridade
                                // Referencial)
                        }
                }
                return "redirect:/sitio/configuracoes";
        }
}