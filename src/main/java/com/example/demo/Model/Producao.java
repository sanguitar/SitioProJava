package com.example.demo.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "producao")
public class Producao {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // AQUI MUDOU: De String para o Objeto Categoria
        @ManyToOne
        @JoinColumn(name = "categoria_id")
        private Categoria categoria;

        private String item;
        private Integer quantidade;
        private String unidade;
        private String status;

        public Producao() {
        }

        public Producao(Long id, Categoria categoria, String item, Integer quantidade, String unidade, String status) {
                this.id = id;
                this.categoria = categoria;
                this.item = item;
                this.quantidade = quantidade;
                this.unidade = unidade;
                this.status = status;
        }

        // Getters e Setters atualizados para Categoria
        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public Categoria getCategoria() {
                return categoria;
        }

        public void setCategoria(Categoria categoria) {
                this.categoria = categoria;
        }

        public String getItem() {
                return item;
        }

        public void setItem(String item) {
                this.item = item;
        }

        public Integer getQuantidade() {
                return quantidade;
        }

        public void setQuantidade(Integer quantidade) {
                this.quantidade = quantidade;
        }

        public String getUnidade() {
                return unidade;
        }

        public void setUnidade(String unidade) {
                this.unidade = unidade;
        }

        public String getStatus() {
                return status;
        }

        public void setStatus(String status) {
                this.status = status;
        }
}