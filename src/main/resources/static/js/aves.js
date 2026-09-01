(function () {
    "use strict";

    const form = document.querySelector("[data-incubation-form]");
    if (!form) return;

    const startInput = form.querySelector("#incubation-start");
    const speciesInput = form.querySelector("#incubation-species");
    const locationInput = form.querySelector("#incubation-location");
    const display = form.querySelector("#incubation-forecast-display");

    function expectedDate() {
        const option = speciesInput.options[speciesInput.selectedIndex];
        const period = Number.parseInt(option ? option.dataset.periodo : "", 10);
        if (!startInput.value || !Number.isFinite(period)) return null;
        const parts = startInput.value.split("-").map(Number);
        const date = new Date(Date.UTC(parts[0], parts[1] - 1, parts[2]));
        date.setUTCDate(date.getUTCDate() + period);
        return date;
    }

    function refreshForecast() {
        const date = expectedDate();
        if (!date) {
            display.textContent = "Período não configurado";
            return;
        }
        display.textContent = new Intl.DateTimeFormat("pt-BR", { timeZone: "UTC" }).format(date);
    }

    function refreshLocations() {
        const method = form.querySelector("input[name='metodo']:checked");
        const incubatorOnly = method && method.value === "CHOCADEIRA";
        Array.from(locationInput.options).forEach(function (option) {
            if (!option.value) return;
            option.disabled = incubatorOnly && option.dataset.tipo !== "INCUBADORA";
        });
        if (locationInput.selectedOptions[0] && locationInput.selectedOptions[0].disabled) {
            locationInput.value = "";
        }
    }

    startInput.addEventListener("change", refreshForecast);
    speciesInput.addEventListener("change", refreshForecast);
    form.querySelectorAll("input[name='metodo']").forEach(function (input) {
        input.addEventListener("change", refreshLocations);
    });
    refreshForecast();
    refreshLocations();
})();
