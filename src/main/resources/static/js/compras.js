(function () {
    "use strict";

    const decimalFormatter = new Intl.NumberFormat("pt-BR", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 4
    });
    const currencyFormatter = new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    });

    function decimalValue(input) {
        if (!input || !input.value) {
            return Number.NaN;
        }
        return Number(input.value.replace(",", "."));
    }

    function officialUnit(form) {
        const itemSelect = form.querySelector("select[name='itemEstoqueId']");
        if (itemSelect) {
            return itemSelect.selectedOptions[0]?.dataset.unidade || "";
        }
        return form.dataset.unidadeOficial || "";
    }

    function updateCalculation(form) {
        const volumes = decimalValue(form.querySelector("[name='quantidadeVolumes']"));
        const content = decimalValue(form.querySelector("[name='conteudoPorVolume']"));
        const price = decimalValue(form.querySelector("[name='precoPorVolume']"));
        const unit = officialUnit(form);
        const quantity = volumes * content;
        const total = volumes * price;

        form.querySelectorAll("[data-unit-output]").forEach((output) => {
            output.textContent = unit || "—";
        });
        form.querySelectorAll("[data-quantity-output]").forEach((output) => {
            output.textContent = Number.isFinite(quantity) && quantity > 0
                ? decimalFormatter.format(quantity)
                : "—";
        });
        form.querySelectorAll("[data-total-output]").forEach((output) => {
            output.textContent = Number.isFinite(total) && total >= 0
                ? currencyFormatter.format(total)
                : currencyFormatter.format(0);
        });
    }

    document.querySelectorAll("[data-compra-item-form]").forEach((form) => {
        form.addEventListener("input", () => updateCalculation(form));
        form.addEventListener("change", () => updateCalculation(form));
        updateCalculation(form);
    });
}());
