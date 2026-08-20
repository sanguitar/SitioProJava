package com.example.sitiopro.frota.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VeiculoFipeDTO {

    private String valor;
    private String ano_modelo;
    private List<HistoricoDTO> historico;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HistoricoDTO {
        private String mes;
        private String valor;

        public String getMes() {
            return mes;
        }

        public void setMes(String mes) {
            this.mes = mes;
        }

        public String getValor() {
            return valor;
        }

        public void setValor(String valor) {
            this.valor = valor;
        }
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getAno_modelo() {
        return ano_modelo;
    }

    public void setAno_modelo(String ano_modelo) {
        this.ano_modelo = ano_modelo;
    }

    public List<HistoricoDTO> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoDTO> historico) {
        this.historico = historico;
    }
}
