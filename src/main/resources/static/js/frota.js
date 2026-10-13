const FIPE_API = "/api/v1/frota/fipe";

document.addEventListener("DOMContentLoaded", () => {
    configurarFormularioFipe();
    configurarMedidor();
});

function configurarFormularioFipe() {
    const tipo = document.getElementById("tipo");
    const marca = document.getElementById("marca");
    const modelo = document.getElementById("modelo");
    const ano = document.getElementById("ano");
    if (!tipo || !marca || !modelo || !ano) return;

    tipo.addEventListener("change", async () => {
        resetSelects([marca, modelo, ano]);
        if (!tipo.value) return;
        popularSelect(marca, await consultar(`${FIPE_API}/marcas?tipo=${tipo.value}`));
    });
    marca.addEventListener("change", async () => {
        resetSelects([modelo, ano]);
        document.getElementById("marcaFipe").value = textoSelecionado(marca);
        if (!marca.value) return;
        popularSelect(modelo, await consultar(`${FIPE_API}/modelos?tipo=${tipo.value}&marca=${marca.value}`));
    });
    modelo.addEventListener("change", async () => {
        resetSelects([ano]);
        document.getElementById("modeloFipe").value = textoSelecionado(modelo);
        if (!modelo.value) return;
        const dados = await consultar(`${FIPE_API}/anos?tipo=${tipo.value}&marca=${marca.value}&modelo=${modelo.value}`);
        popularSelect(ano, dados.map(item => ({id: item.id, nome: `${item.anoModelo} - ${item.combustivel || ""}`})));
    });
    ano.addEventListener("change", async () => {
        if (!ano.value) return;
        const detalhe = await consultar(`${FIPE_API}/detalhes/${ano.value}`);
        document.getElementById("fipeReferenciaId").value = ano.value;
        document.getElementById("anoModelo").value = detalhe.anoModelo ? String(detalhe.anoModelo).substring(0, 4) : "";
    });
}

function configurarMedidor() {
    const tipo = document.getElementById("tipoMedidor");
    if (!tipo) return;
    const atualizar = () => {
        const horimetro = tipo.value === "HORIMETRO";
        document.querySelector("[data-km-group]")?.classList.toggle("d-none", horimetro);
        document.querySelector("[data-horimetro-group]")?.classList.toggle("d-none", !horimetro);
    };
    tipo.addEventListener("change", atualizar);
    atualizar();
}

async function consultar(url) {
    const response = await fetch(url, {headers: {"Accept": "application/json"}});
    if (!response.ok) throw new Error("Consulta FIPE indisponível.");
    return response.json();
}

function popularSelect(element, itens) {
    element.replaceChildren(new Option("Selecione", ""));
    itens.forEach(item => element.appendChild(new Option(item.nome || "", item.id || "")));
    element.disabled = false;
}

function resetSelects(elements) {
    elements.forEach(element => { element.replaceChildren(new Option("Selecione", "")); element.disabled = true; });
}

function textoSelecionado(element) {
    return element.selectedIndex >= 0 ? element.options[element.selectedIndex].text : "";
}
