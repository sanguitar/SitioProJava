document.addEventListener("DOMContentLoaded", () => {
    const iconInput = document.getElementById("hiddenIconInput");
    const selectedIconName = document.getElementById("selectedIconName");

    document.querySelectorAll("[data-icon]").forEach((item) => {
        item.addEventListener("click", () => {
            const iconName = item.dataset.icon;

            if (iconInput) {
                iconInput.value = iconName;
            }
            document.querySelectorAll("[data-icon]").forEach((option) => option.classList.remove("selected"));
            item.classList.add("selected");

            if (selectedIconName) {
                selectedIconName.innerText = `Ícone selecionado: ${iconName}`;
            }
        });
    });
});
