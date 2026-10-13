document.addEventListener("DOMContentLoaded", () => {
    const litros = document.querySelector("[data-litros]");
    const preco = document.querySelector("[data-preco]");
    const total = document.querySelector("[data-total-preview]");
    const veiculo = document.getElementById("veiculo");

    const atualizarTotal = () => {
        if (!litros || !preco || !total) return;
        const valor = Number(litros.value || 0) * Number(preco.value || 0);
        total.textContent = valor.toLocaleString("pt-BR", {style: "currency", currency: "BRL"});
    };
    const atualizarMedidor = () => {
        if (!veiculo) return;
        const option = veiculo.options[veiculo.selectedIndex];
        const horimetro = option?.dataset.medidor === "HORIMETRO";
        const grupoKm = document.querySelector("[data-km-group]");
        const grupoHorimetro = document.querySelector("[data-horimetro-group]");
        grupoKm?.classList.toggle("d-none", horimetro);
        grupoHorimetro?.classList.toggle("d-none", !horimetro);
        const km = grupoKm?.querySelector("input");
        const horas = grupoHorimetro?.querySelector("input");
        if (horimetro && km) km.value = "";
        if (!horimetro && horas) horas.value = "";
    };
    litros?.addEventListener("input", atualizarTotal);
    preco?.addEventListener("input", atualizarTotal);
    veiculo?.addEventListener("change", atualizarMedidor);
    atualizarTotal(); atualizarMedidor();
});
