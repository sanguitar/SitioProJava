package com.example.sitiopro.propriedade.service;

import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.*;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class PerimetroService {
    private final PropriedadeRepository propriedades;
    private final PerimetroPropriedadeRepository perimetros;
    private final PerimetroSpatialRepository spatial;
    private final Validator validator;
    public PerimetroService(PropriedadeRepository propriedades, PerimetroPropriedadeRepository perimetros,
            PerimetroSpatialRepository spatial, Validator validator) {
        this.propriedades = propriedades; this.perimetros = perimetros; this.spatial = spatial; this.validator = validator;
    }
    public PerimetroResumo obter() {
        var p = propriedades.findByPrincipalTrue().orElseThrow(this::ausente);
        return perimetros.findByPropriedadeId(p.getId()).map(this::resumo).orElseGet(PerimetroResumo::vazio);
    }
    public PerimetroRequest formulario() {
        var p = obter(); var r = new PerimetroRequest();
        r.setVersao(p.versao()); r.setStatusCrs(p.statusCrs()); r.setCrs(p.crs());
        r.setDatum(p.datum()); r.setObservacao(p.observacao());
        for (var v : p.vertices()) {
            var item = new VerticePerimetroRequest(); item.setOrdem(v.ordem()); item.setLatitude(v.latitude());
            item.setLongitude(v.longitude()); item.setAltitudeGeodesicaM(v.altitudeGeodesicaM());
            item.setMarco(v.marco()); item.setObservacao(v.observacao());
            r.getVertices().add(item);
        }
        return r;
    }
    @Transactional
    public PerimetroResumo salvar(PerimetroRequest r) {
        validar(r);
        // Lock the existing parent even on first creation, when no perimeter row exists yet.
        var propriedade = propriedades.bloquearPrincipal().orElseThrow(this::ausente);
        if (!propriedade.isAtivo()) throw new PropriedadeOperacaoException(null, "Reative a propriedade antes de alterar o perimetro.");
        var p = perimetros.findByPropriedadeId(propriedade.getId()).orElse(null);
        long atual = p == null ? -1 : p.getVersao();
        if (r.getVersao() != atual) throw new PropriedadeOperacaoException(null,
                "Perimetro alterado. Recarregue os dados antes de salvar.", HttpStatus.CONFLICT);
        if (p == null) { p = new PerimetroPropriedade(); p.setPropriedade(propriedade); }
        var vertices = r.getVertices().stream().sorted(Comparator.comparing(VerticePerimetroRequest::getOrdem))
                .map(v -> new VerticePerimetro(v.getOrdem(), v.getLatitude(), v.getLongitude(),
                        v.getAltitudeGeodesicaM(), texto(v.getMarco()), texto(v.getObservacao()))).toList();
        p.atualizar(r.getStatusCrs(), texto(r.getCrs()), texto(r.getDatum()), texto(r.getObservacao()), vertices);
        var salvo = perimetros.saveAndFlush(p);
        spatial.atualizarRepresentacao(salvo.getId());
        return resumo(salvo);
    }
    private void validar(PerimetroRequest r) {
        var erros = validator.validate(r);
        if (!erros.isEmpty()) {
            var erro = erros.stream().sorted(Comparator.comparing(e -> e.getPropertyPath().toString())).findFirst().orElseThrow();
            throw new PropriedadeOperacaoException(erro.getPropertyPath().toString(), erro.getMessage());
        }
        if (r.getStatusCrs() == StatusCrs.CONFIRMADO && !StringUtils.hasText(r.getCrs()))
            throw new PropriedadeOperacaoException("crs", "Informe o CRS confirmado explicitamente.");
        Set<Integer> ordens = new HashSet<>(); Set<String> pontos = new HashSet<>();
        for (var v : r.getVertices()) {
            if (!ordens.add(v.getOrdem())) throw new PropriedadeOperacaoException(null, "A ordem dos vertices nao pode se repetir.");
            String ponto = v.getLatitude().stripTrailingZeros().toPlainString() + ":" + v.getLongitude().stripTrailingZeros().toPlainString();
            if (!pontos.add(ponto)) throw new PropriedadeOperacaoException(null,
                    "Coordenadas duplicadas. Nao repita o primeiro vertice para fechar o perimetro.");
        }
    }
    private PerimetroResumo resumo(PerimetroPropriedade p) {
        var conferencia = spatial.buscar(p.getId()).orElseGet(() -> new PerimetroConferenciaResumo(
                p.getSistemaGeodesico(), p.getCrsEpsg(), p.getAreaDocumentalHa(), p.getPerimetroDocumentalM(),
                p.getAreaCalculadaM2(), p.getPerimetroCalculadoM(), null, null));
        return new PerimetroResumo(p.getId(), p.getVersao(), p.getStatusCrs(), p.getCrs(), p.getDatum(), p.getObservacao(),
                p.getVertices().stream().sorted(Comparator.comparingInt(VerticePerimetro::getOrdem))
                        .map(v -> new PerimetroResumo.Vertice(v.getOrdem(), v.getLatitude(), v.getLongitude(),
                                v.getAltitudeGeodesicaM(), v.getMarco(), v.getObservacao())).toList(),
                p.getAlteradoEm(), p.getAlteradoPor(), conferencia);
    }
    private String texto(String s) { return StringUtils.hasText(s) ? s.trim() : null; }
    private PropriedadeOperacaoException ausente() {
        return new PropriedadeOperacaoException(null, "Propriedade principal nao encontrada.", HttpStatus.NOT_FOUND);
    }
}
