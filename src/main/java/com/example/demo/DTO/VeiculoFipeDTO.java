package com.example.demo.DTO;

import java.util.List;

public class VeiculoFipeDTO {
    private String valor;
    private String ano_modelo;
    private List<HistoricoDTO> historico; // Aqui está o segredo do gráfico!

    // Classe interna para mapear os meses do histórico
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

    // Getters e Setters do VeiculoFipeDTO
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