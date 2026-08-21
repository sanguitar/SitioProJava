document.addEventListener("DOMContentLoaded", () => {
    const collapsedStorageKey = "sitiopro.sidebar.collapsed";
    const applySidebarState = () => {
        const collapsed = window.localStorage.getItem(collapsedStorageKey) === "true";
        document.body.classList.toggle("sidebar-collapsed", collapsed);
    };

    applySidebarState();

    document.querySelectorAll("[data-sidebar-toggle]").forEach((element) => {
        element.addEventListener("click", () => {
            const nextState = !document.body.classList.contains("sidebar-collapsed");
            document.body.classList.toggle("sidebar-collapsed", nextState);
            window.localStorage.setItem(collapsedStorageKey, String(nextState));
        });
    });

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
