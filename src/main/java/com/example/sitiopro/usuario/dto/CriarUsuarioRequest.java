package com.example.sitiopro.usuario.dto;

import com.example.sitiopro.usuario.entity.PerfilUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CriarUsuarioRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
    private String nome;

    @NotBlank(message = "Login é obrigatório")
    @Size(max = 80, message = "Login deve ter no máximo 80 caracteres")
    private String login;

    @NotBlank(message = "Senha inicial é obrigatória")
    @Size(max = 128, message = "Senha deve ter no máximo 128 caracteres")
    private String senhaInicial;

    @NotNull(message = "Perfil é obrigatório")
    private PerfilUsuario perfil = PerfilUsuario.OPERADOR;

    private boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenhaInicial() {
        return senhaInicial;
    }

    public void setSenhaInicial(String senhaInicial) {
        this.senhaInicial = senhaInicial;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
