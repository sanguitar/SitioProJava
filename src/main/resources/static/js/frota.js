const API_URL = "/api/fipe";

document.addEventListener("DOMContentLoaded", () => {
    configurarFormularioFipe();
    configurarCardsFrota();
});

function configurarFormularioFipe() {
    const tipo = document.getElementById("tipo");
    const marca = document.getElementById("marca");
    const modelo = document.getElementById("modelo");
    const ano = document.getElementById("ano");

    if (!tipo || !marca || !modelo || !ano) {
        return;
    }

    tipo.addEventListener("change", async function () {
        resetCampos(["marca", "modelo", "ano"]);
        if (!this.value) {
            return;
        }

        const data = await fetch(`${API_URL}/marcas?tipo=${this.value}`).then((response) => response.json());
        popularSelect("marca", data);
    });

    marca.addEventListener("change", async function () {
        resetCampos(["modelo", "ano"]);
        const data = await fetch(`${API_URL}/modelos?tipo=${tipo.value}&marca=${this.value}`).then((response) => response.json());
        popularSelect("modelo", data);
    });

    modelo.addEventListener("change", async function () {
        resetCampos(["ano"]);
        const data = await fetch(`${API_URL}/anos?tipo=${tipo.value}&marca=${marca.value}&modelo=${this.value}`).then((response) => response.json());

        ano.innerHTML = '<option value="">Selecione o Ano...</option>';
        data.forEach((veiculo) => {
            ano.insertAdjacentHTML("beforeend", `<option value="${veiculo.id}">${veiculo.ano_modelo} - ${veiculo.combustivel}</option>`);
        });
        ano.disabled = false;
    });

    ano.addEventListener("change", async function () {
        if (!this.value) {
            return;
        }

        const data = await fetch(`${API_URL}/detalhes/${this.value}`).then((response) => response.json());
        document.getElementById("valorFipe").value = data.valor;
        document.getElementById("historicoFipeJson").value = data.historicoJson || JSON.stringify(data.historico);

        const modeloSelecionado = modelo.options[modelo.selectedIndex];
        document.getElementById("nomeSugerido").value = modeloSelecionado ? modeloSelecionado.text : "";
        document.getElementById("secaoFinal").classList.remove("d-none");
    });
}

function configurarCardsFrota() {
    document.querySelectorAll("[data-veiculo-card]").forEach((card) => {
        card.addEventListener("click", () => abrirAnalise(card));
    });
}

function abrirAnalise(card) {
    const historico = card.dataset.historico;
    if (historico) {
        console.info("Histórico FIPE disponível para", card.dataset.nome);
    }
}

function popularSelect(id, lista) {
    const element = document.getElementById(id);
    element.innerHTML = '<option value="">Selecione...</option>';
    lista.forEach((item) => {
        element.insertAdjacentHTML("beforeend", `<option value="${item.id}">${item.nome}</option>`);
    });
    element.disabled = false;
}

function resetCampos(ids) {
    ids.forEach((id) => {
        const element = document.getElementById(id);
        element.innerHTML = "";
        element.disabled = true;
    });
    document.getElementById("secaoFinal")?.classList.add("d-none");
}
