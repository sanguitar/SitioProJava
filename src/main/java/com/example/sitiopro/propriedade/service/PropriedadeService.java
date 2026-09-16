package com.example.sitiopro.propriedade.service;

import com.example.sitiopro.propriedade.dto.*;
import com.example.sitiopro.propriedade.entity.*;
import com.example.sitiopro.propriedade.repository.*;
import com.example.sitiopro.criacao.core.repository.InstalacaoCriacaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.sitiopro.tarefas.dto.PaginaResponse;
import jakarta.persistence.EntityManager;
import jakarta.validation.Validator;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class PropriedadeService {
    private final PropriedadeRepository propriedades;
    private final AreaPropriedadeRepository areas;
    private final TalhaoRepository talhoes;
    private final PiqueteRepository piquetes;
    private final EstruturaPropriedadeRepository estruturas;
    private final RecursoHidricoRepository recursos;
    private final InstalacaoCriacaoRepository instalacoes;
    private final TalhaoSpatialRepository talhaoSpatial;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final EntityManager entityManager;

    public PropriedadeService(PropriedadeRepository propriedades, AreaPropriedadeRepository areas,
            TalhaoRepository talhoes, PiqueteRepository piquetes, EstruturaPropriedadeRepository estruturas,
            RecursoHidricoRepository recursos, InstalacaoCriacaoRepository instalacoes, TalhaoSpatialRepository talhaoSpatial,
            ObjectMapper objectMapper, Validator validator, EntityManager entityManager) {
        this.propriedades = propriedades;
        this.areas = areas;
        this.talhoes = talhoes;
        this.piquetes = piquetes;
        this.estruturas = estruturas;
        this.recursos = recursos;
        this.instalacoes = instalacoes;
        this.talhaoSpatial = talhaoSpatial;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.entityManager = entityManager;
    }

    public Propriedade principal() {
        return propriedades.findByPrincipalTrue().orElseThrow(() ->
                new PropriedadeOperacaoException(null, "Propriedade principal não inicializada.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Propriedade inicializarPrincipal(String nome, BigDecimal latitude, BigDecimal longitude) {
        return propriedades.findByPrincipalTrue().orElseGet(() -> {
            Propriedade p = new Propriedade();
            p.setPrincipal(true);
            p.setNome(nome);
            p.inicializarCoordenadas(latitude, longitude);
            return propriedades.saveAndFlush(p);
        });
    }

    public PropriedadeResumo resumo() {
        Propriedade p = principal();
        return new PropriedadeResumo(p.getId(), p.getNome(), p.getMunicipio(), p.getUf(), p.getAreaTotalHa(),
                p.getLatitudeCentral(), p.getLongitudeCentral(), p.getObservacao(), p.isAtivo(), p.getVersao(),
                areas.countByPropriedadeId(p.getId()), talhoes.countByPropriedadeId(p.getId()),
                piquetes.countByPropriedadeId(p.getId()), estruturas.countByPropriedadeId(p.getId()),
                recursos.countByPropriedadeId(p.getId()));
    }

    public PropriedadeRequest formulario() {
        Propriedade p = principal();
        PropriedadeRequest r = new PropriedadeRequest();
        r.setNome(p.getNome()); r.setMunicipio(p.getMunicipio()); r.setUf(p.getUf());
        r.setAreaTotalHa(p.getAreaTotalHa()); r.setLatitudeCentral(p.getLatitudeCentral());
        r.setLongitudeCentral(p.getLongitudeCentral()); r.setObservacao(p.getObservacao());
        r.setAtivo(p.isAtivo()); r.setVersao(p.getVersao());
        return r;
    }

    @Transactional
    public PropriedadeResumo atualizar(PropriedadeRequest r) {
        validar(r);
        coordenadas(r.getLatitudeCentral(), r.getLongitudeCentral());
        Propriedade p = principal();
        versao(r.getVersao(), p.getVersao());
        p.setNome(r.getNome().trim()); p.setMunicipio(texto(r.getMunicipio())); p.setUf(texto(r.getUf()));
        p.setAreaTotalHa(r.getAreaTotalHa()); p.atualizarCoordenadas(r.getLatitudeCentral(), r.getLongitudeCentral());
        p.setObservacao(texto(r.getObservacao())); p.setAtivo(r.isAtivo());
        propriedades.saveAndFlush(p);
        return resumo();
    }

    @Transactional
    public void atualizarDadosFisicos(String nome, BigDecimal latitude, BigDecimal longitude, Long versao) {
        Propriedade p = principal();
        versao(versao, p.getVersao());
        PropriedadeRequest r = new PropriedadeRequest();
        r.setNome(nome); r.setLatitudeCentral(latitude); r.setLongitudeCentral(longitude);
        validar(r);
        coordenadas(latitude, longitude);
        p.setNome(nome.trim());
        p.atualizarCoordenadas(latitude, longitude);
        propriedades.saveAndFlush(p);
    }

    private Propriedade principalAtiva() {
        Propriedade p = principal();
        if (!p.isAtivo()) throw new PropriedadeOperacaoException(null,
                "Reative a propriedade antes de alterar seus cadastros.");
        return p;
    }

    private void coordenadas(BigDecimal latitude, BigDecimal longitude) {
        if ((latitude == null) != (longitude == null)) throw new PropriedadeOperacaoException(
                latitude == null ? "latitudeCentral" : "longitudeCentral", "Informe as coordenadas em conjunto.");
    }

    public List<CadastroFisicoResumo> areasDisponiveis() {
        return areas.findByPropriedadeIdOrderByNomeAsc(principal().getId()).stream().map(this::resumo).toList();
    }

    public List<CadastroFisicoResumo> estruturasDisponiveis() {
        return estruturas.findByPropriedadeIdOrderByNomeAsc(principal().getId()).stream()
                .filter(EstruturaPropriedade::isAtivo).map(this::resumo).toList();
    }

    public EstruturaPropriedade estruturaParaVinculo(Long id) {
        return estruturas.findByIdAndPropriedadeId(id, principalAtiva().getId())
                .filter(EstruturaPropriedade::isAtivo).orElseThrow(() ->
                        new PropriedadeOperacaoException("estruturaId", "Estrutura não encontrada ou inativa."));
    }

    private AreaPropriedade area(Long id, Propriedade p, AreaPropriedade anterior) {
        if (id == null) return null;
        AreaPropriedade a = areas.findByIdAndPropriedadeId(id, p.getId()).orElseThrow(() ->
                new PropriedadeOperacaoException("areaId", "Área não pertence à propriedade."));
        if (!a.isAtivo() && (anterior == null || !Objects.equals(anterior.getId(), id)))
            throw new PropriedadeOperacaoException("areaId", "Selecione uma área ativa.");
        return a;
    }

    private void validar(Object request) {
        var erros = validator.validate(request);
        if (!erros.isEmpty()) {
            var erro = erros.stream().sorted(java.util.Comparator.comparing(e -> e.getPropertyPath().toString()))
                    .findFirst().orElseThrow();
            throw new PropriedadeOperacaoException(erro.getPropertyPath().toString(), erro.getMessage());
        }
    }

    private void versao(Long esperada, long atual) {
        if (esperada == null || esperada != atual) throw new PropriedadeOperacaoException(null,
                "Este registro foi alterado. Recarregue a página antes de salvar.", HttpStatus.CONFLICT);
    }

    private String texto(String valor) { return StringUtils.hasText(valor) ? valor.trim() : null; }
    private PageRequest pagina(int pagina, int tamanho) { return PageRequest.of(Math.max(0, pagina), Math.min(100, Math.max(1, tamanho))); }
    private PropriedadeOperacaoException ausente() {
        return new PropriedadeOperacaoException(null, "Registro não encontrado nesta propriedade.", HttpStatus.NOT_FOUND);
    }
    private void nomeDuplicado(boolean existe) {
        if (existe) throw new PropriedadeOperacaoException("nome", "Já existe um registro com esse nome.", HttpStatus.CONFLICT);
    }

    public PaginaResponse<CadastroFisicoResumo> listarAreaPropriedade(int pagina, int tamanho) {
        return PaginaResponse.de(areas.findByPropriedadeIdOrderByNomeAsc(principal().getId(), pagina(pagina, tamanho)).map(this::resumo));
    }

    public CadastroFisicoResumo detalharAreaPropriedade(Long id) {
        return resumo(areas.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente));
    }

    public AreaPropriedadeRequest formularioAreaPropriedade(Long id) {
        AreaPropriedade e = areas.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente);
        AreaPropriedadeRequest r = new AreaPropriedadeRequest();
        r.setNome(e.getNome()); r.setObservacao(e.getObservacao()); r.setVersao(e.getVersao());
        r.setTipo(e.getTipo());
        r.setAreaHa(e.getAreaHa());
        r.setAtivo(e.isAtivo());
        return r;
    }

    @Transactional
    public CadastroFisicoResumo salvarAreaPropriedade(Long id, AreaPropriedadeRequest r) {
        validar(r);
        Propriedade p = principalAtiva();
        AreaPropriedade e = id == null ? new AreaPropriedade() :
                areas.findByIdAndPropriedadeId(id, p.getId()).orElseThrow(this::ausente);
        if (id != null) versao(r.getVersao(), e.getVersao());
        nomeDuplicado(areas.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(p.getId(), r.getNome().trim(), id == null ? -1L : id));
        e.setPropriedade(p); e.setNome(r.getNome().trim()); e.setObservacao(texto(r.getObservacao()));
        e.setTipo(r.getTipo());
        e.setAreaHa(r.getAreaHa());
        e.setAtivo(r.isAtivo());
        areas.saveAndFlush(e);
        return resumo(e);
    }

    @Transactional
    public void desativarAreaPropriedade(Long id, Long versao) {
        AreaPropriedadeRequest r = formularioAreaPropriedade(id);
        r.setVersao(versao);
        r.setAtivo(false);
        salvarAreaPropriedade(id, r);
    }

    private CadastroFisicoResumo resumo(AreaPropriedade e) {
        return new CadastroFisicoResumo(e.getId(), e.getPropriedade().getId(),
                null, null,
                null, e.getNome(), e.getTipo().name(),
                e.getAreaHa(),
                null, null,
                null, e.getObservacao(),
                e.isAtivo(), e.isAtivo() ? "ATIVO" : "INATIVO", e.getVersao());
    }

    public PaginaResponse<CadastroFisicoResumo> listarTalhao(int pagina, int tamanho) {
        return PaginaResponse.de(talhoes.findByPropriedadeIdOrderByNomeAsc(principal().getId(), pagina(pagina, tamanho)).map(this::resumo));
    }

    public CadastroFisicoResumo detalharTalhao(Long id) {
        return resumo(talhoes.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente));
    }

    public TalhaoRequest formularioTalhao(Long id) {
        Talhao e = talhoes.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente);
        TalhaoRequest r = new TalhaoRequest();
        r.setNome(e.getNome()); r.setObservacao(e.getObservacao()); r.setVersao(e.getVersao());
        r.setAreaId(e.getArea() == null ? null : e.getArea().getId());
        r.setAreaHa(e.getAreaHa());
        r.setStatus(e.getStatus());
        r.setVertices(e.getVertices().stream().map(v -> {
            var item = new VerticeTalhaoRequest();
            item.setOrdem(v.getOrdem()); item.setLatitude(v.getLatitude()); item.setLongitude(v.getLongitude());
            item.setAltitudeGeodesicaM(v.getAltitudeGeodesicaM()); item.setMarco(v.getMarco());
            item.setObservacao(v.getObservacao());
            return item;
        }).toList());
        return r;
    }

    @Transactional
    public CadastroFisicoResumo salvarTalhao(Long id, TalhaoRequest r) {
        validar(r);
        Propriedade p = principalAtiva();
        Talhao e = id == null ? new Talhao() :
                talhoes.findByIdAndPropriedadeId(id, p.getId()).orElseThrow(this::ausente);
        if (id != null) versao(r.getVersao(), e.getVersao());
        nomeDuplicado(talhoes.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(p.getId(), r.getNome().trim(), id == null ? -1L : id));
        e.setPropriedade(p); e.setNome(r.getNome().trim()); e.setObservacao(texto(r.getObservacao()));
        e.setArea(area(r.getAreaId(), p, e.getArea()));
        e.setAreaHa(r.getAreaHa());
        e.setStatus(r.getStatus());
        e.substituirVertices(verticesTalhao(r));
        talhoes.saveAndFlush(e);
        talhaoSpatial.validarEAtualizarRepresentacao(e.getId());
        entityManager.refresh(e);
        return resumo(e);
    }

    public java.util.List<TalhaoMapaResumo> mapaTalhoes() {
        return talhoes.findByPropriedadeIdOrderByNomeAsc(principal().getId()).stream().map(this::mapaTalhao).toList();
    }

    public TalhoesGeoJsonResumo geoJsonTalhoes() {
        return TalhoesGeoJsonResumo.de(mapaTalhoes());
    }

    public TalhaoGeoJsonImportacaoPreview previewImportacaoTalhoesGeoJson(String geoJson) {
        Propriedade p = principal();
        List<GeoJsonTalhaoImportado> importados = lerTalhoesGeoJson(geoJson);
        List<TalhaoGeoJsonImportacaoPreview.Item> itens = new ArrayList<>();
        for (GeoJsonTalhaoImportado importado : importados) {
            itens.add(previewItem(p.getId(), importado));
        }
        long alterados = itens.stream().filter(TalhaoGeoJsonImportacaoPreview.Item::alterado).count();
        boolean valido = !itens.isEmpty() && itens.stream().allMatch(TalhaoGeoJsonImportacaoPreview.Item::valido);
        return new TalhaoGeoJsonImportacaoPreview(itens, itens.size(), Math.toIntExact(alterados), valido);
    }

    @Transactional
    public TalhaoGeoJsonImportacaoPreview confirmarImportacaoTalhoesGeoJson(TalhaoGeoJsonImportacaoRequest request) {
        validar(request);
        TalhaoGeoJsonImportacaoPreview preview = previewImportacaoTalhoesGeoJson(request.getGeoJson());
        if (preview.possuiErros()) {
            throw new PropriedadeOperacaoException("geoJson",
                    "Corrija os erros do preview antes de confirmar a importação.", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Propriedade p = principalAtiva();
        List<GeoJsonTalhaoImportado> importados = lerTalhoesGeoJson(request.getGeoJson());
        for (GeoJsonTalhaoImportado importado : importados) {
            Talhao talhao = talhoes.findByPropriedadeIdAndCodigoIgnoreCase(p.getId(), importado.codigo())
                    .orElseThrow(() -> new PropriedadeOperacaoException("geoJson",
                            "Talhão não encontrado para o código " + importado.codigo() + ".",
                            HttpStatus.UNPROCESSABLE_ENTITY));
            TalhaoRequest atual = formularioTalhao(talhao.getId());
            atual.setVertices(importado.vertices());
            salvarTalhao(talhao.getId(), atual);
        }
        return previewImportacaoTalhoesGeoJson(request.getGeoJson());
    }

    private TalhaoMapaResumo mapaTalhao(Talhao e) {
        return TalhaoMapaResumo.de(e.getId(), e.getCodigo(), e.getNome(), e.getAreaHa(), e.getAreaGisM2(),
                e.getAreaGisM2() != null, e.getVertices().stream()
                        .sorted(java.util.Comparator.comparingInt(VerticeTalhao::getOrdem))
                        .map(v -> new TalhaoMapaResumo.Vertice(v.getOrdem(), v.getLatitude(), v.getLongitude(),
                                v.getAltitudeGeodesicaM(), v.getMarco(), v.getObservacao()))
                .toList());
    }

    private TalhaoGeoJsonImportacaoPreview.Item previewItem(Long propriedadeId, GeoJsonTalhaoImportado importado) {
        List<String> erros = new ArrayList<>();
        Talhao talhao = talhoes.findByPropriedadeIdAndCodigoIgnoreCase(propriedadeId, importado.codigo()).orElse(null);
        BigDecimal areaImportada = null;
        if (talhao == null) {
            erros.add("Talhão não encontrado para o código informado.");
        }
        try {
            validarVerticesImportados(importado.vertices());
            areaImportada = talhaoSpatial.validarGeometria(propriedadeId, importado.vertices());
        } catch (PropriedadeOperacaoException ex) {
            erros.add(ex.getMessage());
        }
        List<String> alteracoes = new ArrayList<>();
        boolean alterado = false;
        if (talhao != null) {
            alterado = geometriaAlterada(talhao, importado.vertices());
            if (alterado) {
                alteracoes.add("Geometria será substituída.");
            } else {
                alteracoes.add("Geometria sem alterações detectadas.");
            }
            if (areaImportada != null && (talhao.getAreaGisM2() == null
                    || talhao.getAreaGisM2().compareTo(areaImportada) != 0)) {
                alteracoes.add("Área GIS será recalculada.");
            }
        }
        return new TalhaoGeoJsonImportacaoPreview.Item(importado.codigo(),
                talhao == null ? null : talhao.getId(),
                talhao == null ? null : talhao.getNome(),
                talhao == null ? null : talhao.getAreaHa(),
                talhao == null ? null : talhao.getAreaGisM2(),
                areaImportada,
                talhao == null ? 0 : talhao.getVertices().size(),
                importado.vertices().size(),
                alterado,
                alteracoes,
                erros);
    }

    private void validarVerticesImportados(List<VerticeTalhaoRequest> vertices) {
        TalhaoRequest request = new TalhaoRequest();
        request.setNome("Preview importação QGIS");
        request.setAreaHa(BigDecimal.ONE);
        request.setVertices(vertices);
        verticesTalhao(request);
    }

    private boolean geometriaAlterada(Talhao talhao, List<VerticeTalhaoRequest> importados) {
        List<VerticeTalhao> atuais = talhao.getVertices().stream()
                .sorted(java.util.Comparator.comparingInt(VerticeTalhao::getOrdem)).toList();
        if (atuais.size() != importados.size()) return true;
        for (int i = 0; i < atuais.size(); i++) {
            VerticeTalhao atual = atuais.get(i);
            VerticeTalhaoRequest novo = importados.get(i);
            if (atual.getOrdem() != novo.getOrdem()
                    || atual.getLatitude().compareTo(novo.getLatitude()) != 0
                    || atual.getLongitude().compareTo(novo.getLongitude()) != 0
                    || comparar(atual.getAltitudeGeodesicaM(), novo.getAltitudeGeodesicaM()) != 0) {
                return true;
            }
        }
        return false;
    }

    private int comparar(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        return a.compareTo(b);
    }

    private List<GeoJsonTalhaoImportado> lerTalhoesGeoJson(String geoJson) {
        if (!StringUtils.hasText(geoJson)) {
            throw new PropriedadeOperacaoException("geoJson", "Envie um arquivo GeoJSON.");
        }
        try {
            JsonNode raiz = objectMapper.readTree(geoJson);
            List<JsonNode> features = features(raiz);
            if (features.isEmpty()) {
                throw new PropriedadeOperacaoException("geoJson", "GeoJSON sem features de talhão.");
            }
            List<GeoJsonTalhaoImportado> resultado = new ArrayList<>();
            java.util.Set<String> codigos = new java.util.HashSet<>();
            for (JsonNode feature : features) {
                GeoJsonTalhaoImportado importado = feature(feature);
                if (!codigos.add(importado.codigo().toUpperCase(java.util.Locale.ROOT))) {
                    throw new PropriedadeOperacaoException("geoJson",
                            "GeoJSON possui código de talhão duplicado: " + importado.codigo() + ".");
                }
                resultado.add(importado);
            }
            return resultado;
        } catch (IOException ex) {
            throw new PropriedadeOperacaoException("geoJson", "Arquivo GeoJSON inválido.", HttpStatus.BAD_REQUEST);
        }
    }

    private List<JsonNode> features(JsonNode raiz) {
        String tipo = textoJson(raiz.path("type"));
        if ("FeatureCollection".equalsIgnoreCase(tipo)) {
            List<JsonNode> resultado = new ArrayList<>();
            raiz.path("features").forEach(resultado::add);
            return resultado;
        }
        if ("Feature".equalsIgnoreCase(tipo)) {
            return List.of(raiz);
        }
        throw new PropriedadeOperacaoException("geoJson", "Envie um GeoJSON FeatureCollection ou Feature.");
    }

    private GeoJsonTalhaoImportado feature(JsonNode feature) {
        JsonNode propriedades = feature.path("properties");
        validarCrs(propriedades);
        String codigo = textoJson(propriedades.path("codigo"));
        if (!StringUtils.hasText(codigo)) {
            throw new PropriedadeOperacaoException("geoJson", "Cada feature deve informar properties.codigo.");
        }
        JsonNode geometria = feature.path("geometry");
        if (!"Polygon".equalsIgnoreCase(textoJson(geometria.path("type")))) {
            throw new PropriedadeOperacaoException("geoJson", "Somente geometria Polygon é aceita para Talhões.");
        }
        JsonNode anel = geometria.path("coordinates").path(0);
        if (!anel.isArray()) {
            throw new PropriedadeOperacaoException("geoJson", "Polygon sem anel externo válido.");
        }
        List<VerticeTalhaoRequest> vertices = new ArrayList<>();
        int ordem = 1;
        for (JsonNode coordenada : anel) {
            if (!coordenada.isArray() || coordenada.size() < 2) {
                throw new PropriedadeOperacaoException("geoJson", "Coordenada GeoJSON inválida.");
            }
            BigDecimal longitude = decimal(coordenada.get(0));
            BigDecimal latitude = decimal(coordenada.get(1));
            BigDecimal altitude = coordenada.size() >= 3 ? decimal(coordenada.get(2)) : null;
            if (!vertices.isEmpty() && mesmaCoordenada(vertices.getFirst(), latitude, longitude, altitude)
                    && ordem == anel.size()) {
                continue;
            }
            VerticeTalhaoRequest vertice = new VerticeTalhaoRequest();
            vertice.setOrdem(ordem++);
            vertice.setLatitude(latitude);
            vertice.setLongitude(longitude);
            vertice.setAltitudeGeodesicaM(altitude);
            vertices.add(vertice);
        }
        return new GeoJsonTalhaoImportado(codigo.trim(), vertices);
    }

    private boolean mesmaCoordenada(VerticeTalhaoRequest vertice, BigDecimal latitude,
            BigDecimal longitude, BigDecimal altitude) {
        return vertice.getLatitude().compareTo(latitude) == 0
                && vertice.getLongitude().compareTo(longitude) == 0
                && comparar(vertice.getAltitudeGeodesicaM(), altitude) == 0;
    }

    private void validarCrs(JsonNode propriedades) {
        String epsg = textoJson(propriedades.path("epsg"));
        String crs = textoJson(propriedades.path("crs"));
        String datum = textoJson(propriedades.path("datum"));
        boolean epsgOk = "4674".equals(epsg);
        boolean crsOk = "EPSG:4674".equalsIgnoreCase(crs);
        boolean datumOk = "SIRGAS 2000".equalsIgnoreCase(datum);
        if (!epsgOk || !crsOk || !datumOk) {
            throw new PropriedadeOperacaoException("geoJson",
                    "GeoJSON deve declarar properties.epsg=4674, properties.crs=EPSG:4674 e properties.datum=SIRGAS 2000.");
        }
    }

    private BigDecimal decimal(JsonNode node) {
        if (node == null || !node.isNumber()) {
            throw new PropriedadeOperacaoException("geoJson", "Coordenada GeoJSON deve ser numérica.");
        }
        return node.decimalValue();
    }

    private String textoJson(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        return node.asText();
    }

    private record GeoJsonTalhaoImportado(String codigo, List<VerticeTalhaoRequest> vertices) {}

    private java.util.List<VerticeTalhao> verticesTalhao(TalhaoRequest r) {
        java.util.List<VerticeTalhaoRequest> vertices = r.getVertices() == null ? java.util.List.of() : r.getVertices();
        java.util.Set<Integer> ordens = new java.util.HashSet<>();
        java.util.Set<String> coordenadas = new java.util.HashSet<>();
        java.util.List<VerticeTalhao> resultado = new java.util.ArrayList<>();
        for (VerticeTalhaoRequest v : vertices.stream()
                .sorted(java.util.Comparator.comparing(VerticeTalhaoRequest::getOrdem,
                        java.util.Comparator.nullsLast(Integer::compareTo))).toList()) {
            if (v.getOrdem() == null || v.getLatitude() == null || v.getLongitude() == null) {
                continue;
            }
            if (v.getLatitude().compareTo(java.math.BigDecimal.valueOf(-90)) < 0
                    || v.getLatitude().compareTo(java.math.BigDecimal.valueOf(90)) > 0) {
                throw new PropriedadeOperacaoException("vertices", "Latitude do talhão fora do intervalo permitido.");
            }
            if (v.getLongitude().compareTo(java.math.BigDecimal.valueOf(-180)) < 0
                    || v.getLongitude().compareTo(java.math.BigDecimal.valueOf(180)) > 0) {
                throw new PropriedadeOperacaoException("vertices", "Longitude do talhão fora do intervalo permitido.");
            }
            if (!ordens.add(v.getOrdem())) {
                throw new PropriedadeOperacaoException("vertices", "Não repita a ordem dos vértices do talhão.");
            }
            String chave = v.getLatitude().stripTrailingZeros().toPlainString() + "|"
                    + v.getLongitude().stripTrailingZeros().toPlainString();
            if (!coordenadas.add(chave)) {
                throw new PropriedadeOperacaoException("vertices",
                        "Coordenadas duplicadas. Não repita o primeiro vértice para fechar o talhão.");
            }
            resultado.add(new VerticeTalhao(v.getOrdem(), v.getLatitude(), v.getLongitude(),
                    v.getAltitudeGeodesicaM(), texto(v.getMarco()), texto(v.getObservacao())));
        }
        if (!resultado.isEmpty() && resultado.size() < 3) {
            throw new PropriedadeOperacaoException("vertices", "Informe pelo menos três vértices para georreferenciar o talhão.");
        }
        return resultado;
    }

    @Transactional
    public void desativarTalhao(Long id, Long versao) {
        TalhaoRequest r = formularioTalhao(id);
        r.setVersao(versao);
        r.setStatus(StatusDivisaoFisica.INATIVO);
        salvarTalhao(id, r);
    }

    private CadastroFisicoResumo resumo(Talhao e) {
        return new CadastroFisicoResumo(e.getId(), e.getPropriedade().getId(),
                e.getArea() == null ? null : e.getArea().getId(), e.getArea() == null ? null : e.getArea().getNome(),
                e.getCodigo(), e.getNome(), null,
                e.getAreaHa(),
                null, null,
                null, e.getObservacao(),
                e.getStatus() == StatusDivisaoFisica.ATIVO, e.getStatus().name(), e.getVersao(),
                e.getAreaGisM2(), e.getVertices().size());
    }

    public PaginaResponse<CadastroFisicoResumo> listarPiquete(int pagina, int tamanho) {
        return PaginaResponse.de(piquetes.findByPropriedadeIdOrderByNomeAsc(principal().getId(), pagina(pagina, tamanho)).map(this::resumo));
    }

    public CadastroFisicoResumo detalharPiquete(Long id) {
        return resumo(piquetes.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente));
    }

    public PiqueteRequest formularioPiquete(Long id) {
        Piquete e = piquetes.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente);
        PiqueteRequest r = new PiqueteRequest();
        r.setNome(e.getNome()); r.setObservacao(e.getObservacao()); r.setVersao(e.getVersao());
        r.setAreaId(e.getArea() == null ? null : e.getArea().getId());
        r.setAreaHa(e.getAreaHa());
        r.setStatus(e.getStatus());
        return r;
    }

    @Transactional
    public CadastroFisicoResumo salvarPiquete(Long id, PiqueteRequest r) {
        validar(r);
        Propriedade p = principalAtiva();
        Piquete e = id == null ? new Piquete() :
                piquetes.findByIdAndPropriedadeId(id, p.getId()).orElseThrow(this::ausente);
        if (id != null) versao(r.getVersao(), e.getVersao());
        nomeDuplicado(piquetes.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(p.getId(), r.getNome().trim(), id == null ? -1L : id));
        e.setPropriedade(p); e.setNome(r.getNome().trim()); e.setObservacao(texto(r.getObservacao()));
        e.setArea(area(r.getAreaId(), p, e.getArea()));
        e.setAreaHa(r.getAreaHa());
        e.setStatus(r.getStatus());
        piquetes.saveAndFlush(e);
        entityManager.refresh(e);
        return resumo(e);
    }

    @Transactional
    public void desativarPiquete(Long id, Long versao) {
        PiqueteRequest r = formularioPiquete(id);
        r.setVersao(versao);
        r.setStatus(StatusDivisaoFisica.INATIVO);
        salvarPiquete(id, r);
    }

    private CadastroFisicoResumo resumo(Piquete e) {
        return new CadastroFisicoResumo(e.getId(), e.getPropriedade().getId(),
                e.getArea() == null ? null : e.getArea().getId(), e.getArea() == null ? null : e.getArea().getNome(),
                e.getCodigo(), e.getNome(), null,
                e.getAreaHa(),
                null, null,
                null, e.getObservacao(),
                e.getStatus() == StatusDivisaoFisica.ATIVO, e.getStatus().name(), e.getVersao());
    }

    public PaginaResponse<CadastroFisicoResumo> listarEstruturaPropriedade(int pagina, int tamanho) {
        return PaginaResponse.de(estruturas.findByPropriedadeIdOrderByNomeAsc(principal().getId(), pagina(pagina, tamanho)).map(this::resumo));
    }

    public CadastroFisicoResumo detalharEstruturaPropriedade(Long id) {
        return resumo(estruturas.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente));
    }

    public EstruturaPropriedadeRequest formularioEstruturaPropriedade(Long id) {
        EstruturaPropriedade e = estruturas.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente);
        EstruturaPropriedadeRequest r = new EstruturaPropriedadeRequest();
        r.setNome(e.getNome()); r.setObservacao(e.getObservacao()); r.setVersao(e.getVersao());
        r.setAreaId(e.getArea() == null ? null : e.getArea().getId());
        r.setTipo(e.getTipo());
        r.setCapacidade(e.getCapacidade());
        r.setUnidadeCapacidade(e.getUnidadeCapacidade());
        r.setAtivo(e.isAtivo());
        return r;
    }

    @Transactional
    public CadastroFisicoResumo salvarEstruturaPropriedade(Long id, EstruturaPropriedadeRequest r) {
        validar(r);
        Propriedade p = principalAtiva();
        EstruturaPropriedade e = id == null ? new EstruturaPropriedade() :
                estruturas.findByIdAndPropriedadeId(id, p.getId()).orElseThrow(this::ausente);
        if (id != null) versao(r.getVersao(), e.getVersao());
        nomeDuplicado(estruturas.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(p.getId(), r.getNome().trim(), id == null ? -1L : id));
        if ((r.getCapacidade() == null) != (texto(r.getUnidadeCapacidade()) == null))
            throw new PropriedadeOperacaoException("unidadeCapacidade", "Informe capacidade e unidade em conjunto.");
        if (!r.isAtivo() && id != null && instalacoes.existsByEstruturaIdAndAtivoTrue(id))
            throw new PropriedadeOperacaoException(null, "Desvincule as instalações ativas antes de desativar a estrutura.");
        e.setPropriedade(p); e.setNome(r.getNome().trim()); e.setObservacao(texto(r.getObservacao()));
        e.setArea(area(r.getAreaId(), p, e.getArea()));
        e.setTipo(r.getTipo());
        e.setCapacidade(r.getCapacidade());
        e.setUnidadeCapacidade(texto(r.getUnidadeCapacidade()));
        e.setAtivo(r.isAtivo());
        estruturas.saveAndFlush(e);
        return resumo(e);
    }

    @Transactional
    public void desativarEstruturaPropriedade(Long id, Long versao) {
        EstruturaPropriedadeRequest r = formularioEstruturaPropriedade(id);
        r.setVersao(versao);
        r.setAtivo(false);
        salvarEstruturaPropriedade(id, r);
    }

    private CadastroFisicoResumo resumo(EstruturaPropriedade e) {
        return new CadastroFisicoResumo(e.getId(), e.getPropriedade().getId(),
                e.getArea() == null ? null : e.getArea().getId(), e.getArea() == null ? null : e.getArea().getNome(),
                null, e.getNome(), e.getTipo().name(),
                null,
                e.getCapacidade(), e.getUnidadeCapacidade(),
                null, e.getObservacao(),
                e.isAtivo(), e.isAtivo() ? "ATIVO" : "INATIVO", e.getVersao());
    }

    public PaginaResponse<CadastroFisicoResumo> listarRecursoHidrico(int pagina, int tamanho) {
        return PaginaResponse.de(recursos.findByPropriedadeIdOrderByNomeAsc(principal().getId(), pagina(pagina, tamanho)).map(this::resumo));
    }

    public CadastroFisicoResumo detalharRecursoHidrico(Long id) {
        return resumo(recursos.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente));
    }

    public RecursoHidricoRequest formularioRecursoHidrico(Long id) {
        RecursoHidrico e = recursos.findByIdAndPropriedadeId(id, principal().getId()).orElseThrow(this::ausente);
        RecursoHidricoRequest r = new RecursoHidricoRequest();
        r.setNome(e.getNome()); r.setObservacao(e.getObservacao()); r.setVersao(e.getVersao());
        r.setAreaId(e.getArea() == null ? null : e.getArea().getId());
        r.setTipo(e.getTipo());
        r.setCapacidadeLitros(e.getCapacidadeLitros());
        r.setAtivo(e.isAtivo());
        return r;
    }

    @Transactional
    public CadastroFisicoResumo salvarRecursoHidrico(Long id, RecursoHidricoRequest r) {
        validar(r);
        Propriedade p = principalAtiva();
        RecursoHidrico e = id == null ? new RecursoHidrico() :
                recursos.findByIdAndPropriedadeId(id, p.getId()).orElseThrow(this::ausente);
        if (id != null) versao(r.getVersao(), e.getVersao());
        nomeDuplicado(recursos.existsByPropriedadeIdAndNomeIgnoreCaseAndIdNot(p.getId(), r.getNome().trim(), id == null ? -1L : id));
        e.setPropriedade(p); e.setNome(r.getNome().trim()); e.setObservacao(texto(r.getObservacao()));
        e.setArea(area(r.getAreaId(), p, e.getArea()));
        e.setTipo(r.getTipo());
        e.setCapacidadeLitros(r.getCapacidadeLitros());
        e.setAtivo(r.isAtivo());
        recursos.saveAndFlush(e);
        return resumo(e);
    }

    @Transactional
    public void desativarRecursoHidrico(Long id, Long versao) {
        RecursoHidricoRequest r = formularioRecursoHidrico(id);
        r.setVersao(versao);
        r.setAtivo(false);
        salvarRecursoHidrico(id, r);
    }

    private CadastroFisicoResumo resumo(RecursoHidrico e) {
        return new CadastroFisicoResumo(e.getId(), e.getPropriedade().getId(),
                e.getArea() == null ? null : e.getArea().getId(), e.getArea() == null ? null : e.getArea().getNome(),
                null, e.getNome(), e.getTipo().name(),
                null,
                null, null,
                e.getCapacidadeLitros(), e.getObservacao(),
                e.isAtivo(), e.isAtivo() ? "ATIVO" : "INATIVO", e.getVersao());
    }
}
