document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-perimeter-map]").forEach((container) => {
        renderPerimeterMap(container).catch(() => {
            setMapState(container, "Não foi possível carregar o mapa. A lista de vértices continua disponível.");
        });
    });
});

async function renderPerimeterMap(container) {
    const response = await fetch(container.dataset.perimeterApi, {
        headers: { Accept: "application/json" },
        credentials: "same-origin",
    });
    if (!response.ok) {
        throw new Error("Perimeter API unavailable");
    }
    const data = await response.json();
    const mapa = data.mapa || {};
    const vertices = Array.isArray(mapa.vertices) ? mapa.vertices : [];
    const layer = container.querySelector("[data-map-layer]");
    layer.replaceChildren();

    if (vertices.length === 0) {
        setMapState(container, "Sem vértices cadastrados.");
        return;
    }

    const projected = project(vertices);
    if (projected.length === 1) {
        drawMarker(layer, projected[0]);
        setMapState(container, "Um vértice cadastrado.");
        return;
    }

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

    const suffix = data.statusCrs === "CONFIRMADO" ? "CRS confirmado." : "CRS não confirmado.";
    setMapState(container, `${vertices.length} vértices exibidos na ordem cadastrada. ${suffix}`);
}

function project(vertices) {
    const width = 900;
    const height = 520;
    const padding = 56;
    const lats = vertices.map((v) => Number(v.latitude));
    const lons = vertices.map((v) => Number(v.longitude));
    const minLat = Math.min(...lats);
    const maxLat = Math.max(...lats);
    const minLon = Math.min(...lons);
    const maxLon = Math.max(...lons);
    const latRange = Math.max(maxLat - minLat, 0.000001);
    const lonRange = Math.max(maxLon - minLon, 0.000001);
    return vertices.map((v) => ({
        x: padding + ((Number(v.longitude) - minLon) / lonRange) * (width - padding * 2),
        y: height - padding - ((Number(v.latitude) - minLat) / latRange) * (height - padding * 2),
        ordem: v.ordem,
        rotulo: v.rotulo || String(v.ordem),
    }));
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
    if (state) {
        state.textContent = message;
    }
}
