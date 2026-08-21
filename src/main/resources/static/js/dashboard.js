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
                backgroundColor: ["#173F35", "#2F6B4F", "#78A97A", "#D6A63C", "#8A6042", "#1C6B8C", "#20282A"],
                hoverOffset: 8,
                borderColor: "#F4F0E6",
                borderWidth: 3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: "bottom",
                    labels: {
                        color: "#20282A",
                        boxWidth: 12,
                        padding: 18
                    }
                }
            }
        }
    });
});
