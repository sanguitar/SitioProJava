package com.example.sitiopro.compras.dto;

import com.example.sitiopro.compras.entity.StatusCompra;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class CompraFiltro {

    private StatusCompra status;

    private Long fornecedorId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate inicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fim;

    public StatusCompra getStatus() {
        return status;
    }

    public void setStatus(StatusCompra status) {
        this.status = status;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }
}
