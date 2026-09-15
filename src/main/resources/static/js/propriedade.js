document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-perimeter-map]").forEach((container) => {
        renderPerimeterMap(container).catch(() => {
            renderSvgFallback(container).catch(() => {
                setMapState(container, "Não foi possível carregar o mapa. A lista de vértices continua disponível.");
            });
        });
    });
});

async function renderPerimeterMap(container) {
    const data = await fetchPerimeter(container);
    if (!window.ol || !window.proj4) {
        await renderSvgFallback(container, data);
        return;
    }
    registrarSirgas2000();

    const mapa = data.mapa || {};
    const vertices = Array.isArray(mapa.vertices) ? mapa.vertices : [];
    const talhoes = Array.isArray(mapa.talhoes) ? mapa.talhoes : [];
    const target = container.querySelector("[data-ol-map]");
    if (!target || (vertices.length === 0 && talhoes.length === 0)) {
        setMapState(container, "Sem vértices cadastrados.");
        return;
    }

    const perimetroSource = new ol.source.Vector();
    const talhaoSource = new ol.source.Vector();
    const marcadorSource = new ol.source.Vector();

    if (vertices.length >= 3) {
        perimetroSource.addFeature(polygonFeature(vertices, {
            tipo: "Perímetro oficial",
            nome: "Perímetro da propriedade",
            cor: "#17663a",
        }));
    }
    talhoes.filter((talhao) => Array.isArray(talhao.vertices) && talhao.vertices.length >= 3)
            .forEach((talhao) => talhaoSource.addFeature(polygonFeature(talhao.vertices, {
                tipo: "Talhão",
                nome: talhao.nome,
                codigo: talhao.codigo || "",
                areaGisM2: talhao.areaGisM2 || "",
                cor: talhao.cor || "#2f80ed",
            })));
    vertices.forEach((v) => marcadorSource.addFeature(pointFeature(v, "Perímetro")));
    talhoes.forEach((talhao) => (talhao.vertices || []).forEach((v) =>
        marcadorSource.addFeature(pointFeature(v, talhao.codigo || talhao.nome || "Talhão"))));

    const controls = ol.control.defaults
        ? ol.control.defaults().extend([new ol.control.ScaleLine()])
        : undefined;

    const map = new ol.Map({
        target,
        layers: [
            new ol.layer.Tile({ source: new ol.source.OSM() }),
            new ol.layer.Vector({ source: perimetroSource, style: perimeterStyle }),
            new ol.layer.Vector({ source: talhaoSource, style: talhaoStyle }),
            new ol.layer.Vector({ source: marcadorSource, style: markerStyle }),
        ],
        view: new ol.View({ center: ol.proj.fromLonLat([-63.8713, -8.3487]), zoom: 17 }),
        controls,
    });

    const extent = combinedExtent([perimetroSource, talhaoSource, marcadorSource]);
    const center = () => fitExtent(map, extent);
    center();
    container.closest(".property-section")?.querySelector("[data-center-property]")
            ?.addEventListener("click", center);
    configurePopup(container, map);

    setMapState(container, `${vertices.length} vértices do perímetro e ${talhoes.length} talhões sobre mapa-base cartográfico. EPSG:4674 preservado no banco.`);
}

async function fetchPerimeter(container) {
    const response = await fetch(container.dataset.perimeterApi, {
        headers: { Accept: "application/json" },
        credentials: "same-origin",
    });
    if (!response.ok) throw new Error("Perimeter API unavailable");
    return response.json();
}

function registrarSirgas2000() {
    if (!proj4.defs("EPSG:4674")) {
        proj4.defs("EPSG:4674", "+proj=longlat +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +no_defs +type=crs");
    }
    if (!proj4.defs("EPSG:3857")) {
        proj4.defs("EPSG:3857", "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +k=1.0 +units=m +nadgrids=@null +wktext +no_defs +type=crs");
    }
}

function toWebMercator(point) {
    const lonLat = [Number(point.longitude), Number(point.latitude)];
    if (Number.isNaN(lonLat[0]) || Number.isNaN(lonLat[1])) return null;
    return proj4("EPSG:4674", "EPSG:3857", lonLat);
}

function polygonFeature(vertices, props) {
    const coords = vertices.map(toWebMercator).filter(Boolean);
    coords.push(coords[0]);
    return new ol.Feature({ geometry: new ol.geom.Polygon([coords]), ...props });
}

function pointFeature(vertex, origem) {
    return new ol.Feature({
        geometry: new ol.geom.Point(toWebMercator(vertex)),
        tipo: "Vértice",
        nome: vertex.rotulo || `${origem} ${vertex.ordem}`,
        origem,
        ordem: vertex.ordem,
        latitude: vertex.latitude,
        longitude: vertex.longitude,
        altitude: vertex.altitudeGeodesicaM || "",
    });
}

function perimeterStyle() {
    return new ol.style.Style({
        stroke: new ol.style.Stroke({ color: "#17663a", width: 4 }),
        fill: new ol.style.Fill({ color: "rgba(23, 102, 58, 0.18)" }),
    });
}

function talhaoStyle(feature) {
    const color = feature.get("cor") || "#2f80ed";
    return new ol.style.Style({
        stroke: new ol.style.Stroke({ color, width: 2.5 }),
        fill: new ol.style.Fill({ color: hexToRgba(color, 0.24) }),
    });
}

function markerStyle(feature) {
    return new ol.style.Style({
        image: new ol.style.Circle({
            radius: 6,
            fill: new ol.style.Fill({ color: "#ffffff" }),
            stroke: new ol.style.Stroke({ color: "#173d2a", width: 2 }),
        }),
        text: new ol.style.Text({
            text: String(feature.get("ordem") || ""),
            offsetY: -16,
            font: "700 12px system-ui, sans-serif",
            fill: new ol.style.Fill({ color: "#173d2a" }),
            stroke: new ol.style.Stroke({ color: "#ffffff", width: 4 }),
        }),
    });
}

function combinedExtent(sources) {
    return sources.map((source) => source.getExtent())
            .filter((extent) => extent && !ol.extent.isEmpty(extent))
            .reduce((acc, extent) => acc ? ol.extent.extend(acc, extent) : extent.slice(), null);
}

function fitExtent(map, extent) {
    if (extent) map.getView().fit(extent, { padding: [46, 46, 46, 46], maxZoom: 20, duration: 250 });
}

function configurePopup(container, map) {
    const popup = container.querySelector("[data-map-popup]");
    map.on("singleclick", (event) => {
        const feature = map.forEachFeatureAtPixel(event.pixel, (item) => item);
        if (!feature || !popup) {
            popup?.classList.remove("is-visible");
            return;
        }
        popup.innerHTML = popupHtml(feature);
        popup.style.left = `${event.pixel[0]}px`;
        popup.style.top = `${event.pixel[1]}px`;
        popup.classList.add("is-visible");
    });
    map.on("pointermove", (event) => {
        map.getTargetElement().style.cursor = map.hasFeatureAtPixel(event.pixel) ? "pointer" : "";
    });
}

function popupHtml(feature) {
    const tipo = escapeHtml(feature.get("tipo") || "Mapa");
    const nome = escapeHtml(feature.get("nome") || "");
    const lat = feature.get("latitude");
    const lon = feature.get("longitude");
    const altitude = feature.get("altitude");
    const area = feature.get("areaGisM2");
    let html = `<strong>${tipo}</strong><div>${nome}</div>`;
    if (lon && lat) html += `<div>Lon ${escapeHtml(lon)}<br>Lat ${escapeHtml(lat)}</div>`;
    if (altitude) html += `<div>Alt ${escapeHtml(altitude)} m</div>`;
    if (area) html += `<div>Área GIS ${escapeHtml(area)} m²</div>`;
    html += "<div>EPSG:4674 SIRGAS 2000</div>";
    return html;
}

function hexToRgba(hex, alpha) {
    const clean = String(hex).replace("#", "");
    if (clean.length !== 6) return `rgba(47, 128, 237, ${alpha})`;
    const value = Number.parseInt(clean, 16);
    return `rgba(${(value >> 16) & 255}, ${(value >> 8) & 255}, ${value & 255}, ${alpha})`;
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, (char) => ({
        "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;",
    }[char]));
}

async function renderSvgFallback(container, preloadedData) {
    const data = preloadedData || await fetchPerimeter(container);
    const target = container.querySelector("[data-ol-map]");
    if (target) target.hidden = true;
    const svg = container.querySelector("[data-svg-fallback]");
    if (svg) svg.hidden = false;

    const mapa = data.mapa || {};
    const vertices = Array.isArray(mapa.vertices) ? mapa.vertices : [];
    const talhoes = Array.isArray(mapa.talhoes) ? mapa.talhoes : [];
    const layer = container.querySelector("[data-map-layer]");
    layer.replaceChildren();

    if (vertices.length === 0 && talhoes.length === 0) {
        setMapState(container, "Sem vértices cadastrados.");
        return;
    }

    const projected = project(vertices, talhoes);
    projectTalhoes(talhoes, projected.bounds).forEach((talhao) => drawTalhao(layer, talhao));
    drawGrid(layer);
    const linePoints = projected.map((p) => `${p.x},${p.y}`).join(" ");
    if (projected.length >= 3) {
        const polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
        polygon.setAttribute("points", `${linePoints} ${projected[0].x},${projected[0].y}`);
        polygon.setAttribute("class", "perimeter-polygon");
        layer.appendChild(polygon);
    }
    const line = document.createElementNS("http://www.w3.org/2000/svg", "polyline");
    line.setAttribute("points", linePoints);
    line.setAttribute("class", "perimeter-line");
    layer.appendChild(line);
    projected.forEach((point) => drawMarker(layer, point));
    setMapState(container, "Mapa-base indisponível. Exibindo desenho local das coordenadas cadastradas.");
}

function project(vertices, talhoes = []) {
    const width = 900;
    const height = 520;
    const padding = 56;
    const todos = [...vertices, ...talhoes.flatMap((t) => Array.isArray(t.vertices) ? t.vertices : [])];
    const lats = todos.map((v) => Number(v.latitude));
    const lons = todos.map((v) => Number(v.longitude));
    const minLat = Math.min(...lats);
    const maxLat = Math.max(...lats);
    const minLon = Math.min(...lons);
    const maxLon = Math.max(...lons);
    const latRange = Math.max(maxLat - minLat, 0.000001);
    const lonRange = Math.max(maxLon - minLon, 0.000001);
    const bounds = { minLat, minLon, latRange, lonRange, width, height, padding };
    const pontos = vertices.map((v) => ({
        x: padding + ((Number(v.longitude) - minLon) / lonRange) * (width - padding * 2),
        y: height - padding - ((Number(v.latitude) - minLat) / latRange) * (height - padding * 2),
        ordem: v.ordem,
        rotulo: v.rotulo || String(v.ordem),
    }));
    pontos.bounds = bounds;
    return pontos;
}

function projectTalhoes(talhoes, bounds) {
    if (!bounds) return [];
    return talhoes.map((talhao) => ({
        ...talhao,
        pontos: (Array.isArray(talhao.vertices) ? talhao.vertices : []).map((v) => ({
            x: bounds.padding + ((Number(v.longitude) - bounds.minLon) / bounds.lonRange) * (bounds.width - bounds.padding * 2),
            y: bounds.height - bounds.padding - ((Number(v.latitude) - bounds.minLat) / bounds.latRange) * (bounds.height - bounds.padding * 2),
            ordem: v.ordem,
            rotulo: v.rotulo || String(v.ordem),
        })),
    })).filter((talhao) => talhao.pontos.length >= 3);
}

function drawGrid(layer) {
    for (let i = 1; i < 5; i += 1) {
        const vertical = document.createElementNS("http://www.w3.org/2000/svg", "line");
        vertical.setAttribute("x1", String(i * 180));
        vertical.setAttribute("x2", String(i * 180));
        vertical.setAttribute("y1", "24");
        vertical.setAttribute("y2", "496");
        vertical.setAttribute("class", "perimeter-grid");
        layer.appendChild(vertical);

        const horizontal = document.createElementNS("http://www.w3.org/2000/svg", "line");
        horizontal.setAttribute("x1", "24");
        horizontal.setAttribute("x2", "876");
        horizontal.setAttribute("y1", String(i * 104));
        horizontal.setAttribute("y2", String(i * 104));
        horizontal.setAttribute("class", "perimeter-grid");
        layer.appendChild(horizontal);
    }
}

function drawTalhao(layer, talhao) {
    const points = talhao.pontos.map((p) => `${p.x},${p.y}`).join(" ");
    const polygon = document.createElementNS("http://www.w3.org/2000/svg", "polygon");
    polygon.setAttribute("points", points);
    polygon.setAttribute("class", "talhao-polygon");
    polygon.style.setProperty("--talhao-color", talhao.cor || "#2f80ed");
    layer.appendChild(polygon);

    const cx = talhao.pontos.reduce((sum, p) => sum + p.x, 0) / talhao.pontos.length;
    const cy = talhao.pontos.reduce((sum, p) => sum + p.y, 0) / talhao.pontos.length;
    const label = document.createElementNS("http://www.w3.org/2000/svg", "text");
    label.setAttribute("x", String(cx));
    label.setAttribute("y", String(cy));
    label.setAttribute("class", "talhao-label");
    label.textContent = talhao.codigo || talhao.nome || "Talhão";
    layer.appendChild(label);
}

function drawMarker(layer, point) {
    const group = document.createElementNS("http://www.w3.org/2000/svg", "g");
    group.setAttribute("class", "perimeter-marker");
    group.setAttribute("transform", `translate(${point.x} ${point.y})`);

    const circle = document.createElementNS("http://www.w3.org/2000/svg", "circle");
    circle.setAttribute("r", "9");
    group.appendChild(circle);

    const label = document.createElementNS("http://www.w3.org/2000/svg", "text");
    label.setAttribute("x", "14");
    label.setAttribute("y", "-12");
    label.textContent = point.rotulo;
    group.appendChild(label);
    layer.appendChild(group);
}

function setMapState(container, message) {
    const state = container.querySelector("[data-map-state]");
    if (state) state.textContent = message;
}
