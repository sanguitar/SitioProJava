document.addEventListener("DOMContentLoaded", () => {
    const chartElement = document.getElementById("agroChart");

    if (!chartElement || typeof Chart === "undefined") {
        return;
    }

    const labels = JSON.parse(chartElement.dataset.labels || "[]");
    const values = JSON.parse(chartElement.dataset.values || "[]");

    new Chart(chartElement.getContext("2d"), {
        type: "doughnut",
        data: {
            labels,
            datasets: [{
                data: values,
                backgroundColor: ["#1e3d1a", "#8dbd31", "#212529", "#ffc107", "#dc3545", "#0d6efd", "#6c757d"],
                hoverOffset: 15,
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "bottom"
                }
            }
        }
    });
});
