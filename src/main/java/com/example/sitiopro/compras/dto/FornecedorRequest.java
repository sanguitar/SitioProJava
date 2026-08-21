package com.example.sitiopro.compras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FornecedorRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 140, message = "Nome deve ter no máximo 140 caracteres")
    private String nome;

    @Size(max = 40, message = "Documento deve ter no máximo 40 caracteres")
    private String documento;

    @Size(max = 40, message = "Telefone deve ter no máximo 40 caracteres")
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 140, message = "E-mail deve ter no máximo 140 caracteres")
    private String email;

    @Size(max = 500, message = "Observação deve ter no máximo 500 caracteres")
    private String observacao;

    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
