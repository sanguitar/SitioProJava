package com.example.sitiopro.propriedade.dto;

import com.example.sitiopro.propriedade.entity.StatusCrs;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class PerimetroRequest {
    @NotNull @Min(-1) private Long versao;
    @NotNull private StatusCrs statusCrs = StatusCrs.NAO_CONFIRMADO;
    @Size(max = 120) private String crs;
    @Size(max = 120) private String datum;
    @Size(max = 1000) private String observacao;
    @NotNull @Size(max = 500) @Valid
    private List<@NotNull VerticePerimetroRequest> vertices = new ArrayList<>();
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public StatusCrs getStatusCrs() { return statusCrs; }
    public void setStatusCrs(StatusCrs statusCrs) { this.statusCrs = statusCrs; }
    public String getCrs() { return crs; }
    public void setCrs(String crs) { this.crs = crs; }
    public String getDatum() { return datum; }
    public void setDatum(String datum) { this.datum = datum; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public List<VerticePerimetroRequest> getVertices() { return vertices; }
    public void setVertices(List<VerticePerimetroRequest> vertices) { this.vertices = vertices; }
}
