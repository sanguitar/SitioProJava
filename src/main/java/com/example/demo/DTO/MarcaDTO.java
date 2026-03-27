package com.example.demo.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Esta anotação evita erros se a API enviar campos extras que não mapeamos
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarcaDTO {

    private Integer id;
    private String nome;

    // Construtor padrão necessário para o Jackson (JSON)
    public MarcaDTO() {
    }

    public MarcaDTO(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // No seu pacote DTO
    public class AnoDTO {
        public Integer id;
        public String ano_modelo;
        public String combustivel;
        // Getters e Setters...
    }
}