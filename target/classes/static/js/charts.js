/* Chart.js Graphic Visualizations */
function renderResultChart(canvasId, correct, wrong, unanswered) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Correct', 'Wrong', 'Unanswered'],
            datasets: [{
                data: [correct, wrong, unanswered],
                backgroundColor: ['#10b981', '#f43f5e', '#94a3b8'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' }
            },
            cutout: '75%'
        }
    });
}

function renderAnalyticsChart(canvasId, labels, dataPoints) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Average Score (%)',
                data: dataPoints,
                backgroundColor: 'rgba(59, 130, 246, 0.75)',
                borderColor: '#3b82f6',
                borderWidth: 1,
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: { beginAtZero: true, max: 100 }
            }
        }
    });
}
