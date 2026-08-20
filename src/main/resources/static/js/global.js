document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-confirm-message]").forEach((element) => {
        element.addEventListener("click", (event) => {
            if (!window.confirm(element.dataset.confirmMessage)) {
                event.preventDefault();
            }
        });
    });

    document.querySelectorAll("[data-auto-submit]").forEach((element) => {
        element.addEventListener("change", () => {
            element.form?.submit();
        });
    });
});
