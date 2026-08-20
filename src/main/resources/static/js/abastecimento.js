document.addEventListener("DOMContentLoaded", () => {
    const litros = document.querySelector("[data-litros]");
    const preco = document.querySelector("[data-preco]");
    const totalPreview = document.querySelector("[data-total-preview]");

    if (!litros || !preco || !totalPreview) {
        return;
    }

    const atualizarTotal = () => {
        const total = Number(litros.value || 0) * Number(preco.value || 0);
        totalPreview.textContent = total.toLocaleString("pt-BR", {
            style: "currency",
            currency: "BRL"
        });
    };

    litros.addEventListener("input", atualizarTotal);
    preco.addEventListener("input", atualizarTotal);
});
