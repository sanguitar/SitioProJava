package com.example.sitiopro.usuario.mobile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MobileLoginRequest {

    @NotBlank
    @Size(max = 80)
    private String login;

    @NotBlank
    @Size(max = 200)
    private String password;

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
